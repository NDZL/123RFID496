package com.zebra.demo.rfidreader.reader_connection;

import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.READER_LIST_TAB;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.DeviceDiscoverActivity;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CameraScanFragment extends Fragment {

    private static final int CAMERA_PERMISSION_CODE = 100;

    PreviewView previewView;
    CameraSelector cameraSelector;
    int lensFacing = CameraSelector.LENS_FACING_BACK;
    private ProcessCameraProvider cameraProvider;
    Preview previewUseCase;
    ImageAnalysis analysisUseCase;
    Button bScan;
    FrameLayout layoutCamera, layoutScanButton;
    TextView failureText;

    public CameraScanFragment() {

    }

    public static CameraScanFragment newInstance() {

        return new CameraScanFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera_scan, container, false);

        layoutCamera = view.findViewById(R.id.camera_view);
        layoutScanButton = view.findViewById(R.id.button_view);
        failureText = view.findViewById(R.id.failure_text);
        bScan = view.findViewById(R.id.scan_button);
        bScan.setOnClickListener(v -> {
            PackageManager pm = requireActivity().getPackageManager();
            if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                layoutScanButton.setVisibility(View.GONE);
                layoutCamera.setVisibility(View.VISIBLE);
                setupCamera(view);
            }else{
                Toast.makeText(requireActivity(), "Camera not found, this feature not supported", Toast.LENGTH_SHORT).show();
            }

        });
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
               if(cameraProvider != null){
                   cameraProvider.unbindAll();
               }
            }
            return false;
        });

        return view;
    }

    private void setupCamera(View view) {
        previewView = view.findViewById(R.id.preview_view);
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(
                        getViewLifecycleOwner(),
                        provider -> {
                            cameraProvider = provider;
                            if (checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)) {
                                bindCameraUseCases();
                            }
                        });
    }

    private void bindCameraUseCases() {
        cameraProvider.unbindAll();
        bindPreviewUseCase();
        bindAnalyseUseCase();
    }

    private void bindPreviewUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }
        previewUseCase = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(previewView.getDisplay().getRotation())
                .build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase);
        camera.getCameraControl().enableTorch(false);
    }

    private void bindAnalyseUseCase() {

        BarcodeScanner barcodeScanner = BarcodeScanning.getClient();

        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }

        analysisUseCase = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(previewView.getDisplay().getRotation())
                .build();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        analysisUseCase.setAnalyzer(executorService, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                processImageProxy(barcodeScanner, image);

            }
        });
        cameraProvider.bindToLifecycle(this, cameraSelector, analysisUseCase);
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(BarcodeScanner barcodeScanner, ImageProxy image) {
        InputImage inputImage = InputImage.fromMediaImage(Objects.requireNonNull(image.getImage()), image.getImageInfo().getRotationDegrees());

        barcodeScanner.process(inputImage).addOnSuccessListener(barcodes -> {

            if (barcodes.size() > 0) {
                for (Barcode barcode : barcodes) {
                    int scannedFormat = barcode.getFormat();
                    switch (scannedFormat) {
                        case Barcode.FORMAT_CODE_128:
                        case Barcode.FORMAT_DATA_MATRIX:
                            cameraProvider.unbindAll();

                            layoutScanButton.setVisibility(View.VISIBLE);
                            layoutCamera.setVisibility(View.GONE);

                            if (Application.scanPair == null) {
                                Application.scanPair = new ScanPair();

                            }
                            Application.scanPair.Init(getActivity(), this);
                            if (scannedFormat == Barcode.FORMAT_DATA_MATRIX) {
                                if (Objects.requireNonNull(barcode.getRawValue()).startsWith("P")) {
                                    Application.scanPair.barcodeDeviceNameConnect(barcode.getRawValue().substring(1));
                                }else if(barcode.getRawValue().length() == Defines.BT_ADDRESS_LENGTH){
                                    Application.scanPair.barcodeDeviceNameConnect(barcode.getRawValue());
                                }else{
                                    Toast.makeText(getActivity(), barcode.getRawValue() +" is not valid BT address",Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Application.scanPair.barcodeDeviceNameConnect(barcode.getRawValue());
                            }
                            break;
                        default:
                            break;
                    }
                }
            }

        }).addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
            @Override
            public void onComplete(@NonNull Task<List<Barcode>> task) {
                image.close();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("test" , e.toString());
                layoutScanButton.setVisibility(View.VISIBLE);
                layoutCamera.setVisibility(View.GONE);
                cameraProvider.unbindAll();
                image.close();

                Spannable errorMessage = new SpannableString(e.getMessage());
                errorMessage.setSpan(new ForegroundColorSpan(Color.RED), 0, errorMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                failureText.setText(errorMessage);

                Spannable internetConnectionText = new SpannableString(getString(R.string.ml_kit_internet_connection));
                internetConnectionText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, internetConnectionText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                failureText.append("\n\n");
                failureText.append(internetConnectionText);

            }
        });
    }

    public void connectDevice(String rdDevice, boolean b) {
        Toast.makeText(getContext(), "Device ready to connect:" + rdDevice, Toast.LENGTH_SHORT).show();
        Activity activity = getActivity();
        if (activity instanceof DeviceDiscoverActivity) {
            Fragment fragment = InitReadersListFragment.getInstance();
            ((DeviceDiscoverActivity) getActivity()).switchToFragment(fragment);
            ((DeviceDiscoverActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.title_empty_readers));


        } else if (activity instanceof ActiveDeviceActivity) {
            ((ActiveDeviceActivity) getActivity()).loadNextFragment(READER_LIST_TAB);
        }

    }

    public void processCompleted(String message) {

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            });
        }

    }

    public boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(requireActivity(), permission) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{permission}, requestCode);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bindCameraUseCases();
            }
        }
    }

    @Override
    public void onDestroyView() {
        if(cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        super.onDestroyView();
    }

}