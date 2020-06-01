package com.example.idscanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.otaliastudios.cameraview.CameraListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class BarcodeScannerActivity : BaseCameraActivity() {

    private val qrList = arrayListOf<QrCode>()
    val adapter = QrCodeAdapter(qrList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rvQrCode.layoutManager = LinearLayoutManager(this)
        rvQrCode.adapter = adapter

        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)

        exit.setOnClickListener {
            qrList.clear()
            adapter.notifyDataSetChanged()
            exitWrapper.visibility = View.INVISIBLE
        }

        //handle toggle switch for scan
        var scan = false
        switch1.setOnCheckedChangeListener { _, isChecked ->
            scan = true
            if (!isChecked) {
                scan = false
            }
        }

        //camera listener for each picture
        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(jpeg: ByteArray?) {
                val bitmap = jpeg?.size?.let { BitmapFactory.decodeByteArray(jpeg, 0, it) }
                bitmap?.let { runBarcodeScanner(it) }
            }

        })

        //autoScan timer
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (scan) {
                    cameraView.captureSnapshot()
                }
            }
        }, 0, 250)
    }

    private fun runBarcodeScanner(bitmap: Bitmap) {
        //Create a FirebaseVisionImage
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(
                        //Detect PDF417
                        FirebaseVisionBarcode.FORMAT_PDF417

                )
                .build()

        //Get access to an instance of FirebaseBarcodeDetector
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

        //Use the detector to detect the labels inside the image
        detector.detectInImage(image)
                .addOnSuccessListener {

                    // Task completed successfully
                    for (firebaseBarcode in it) {
                        when (firebaseBarcode.valueType) {
                            FirebaseVisionBarcode.TYPE_DRIVER_LICENSE -> {
                                exitWrapper.visibility = View.VISIBLE
                                qrList.clear()
                                qrList.add(QrCode(firebaseBarcode.driverLicense?.licenseNumber, firebaseBarcode.driverLicense?.firstName + " " + firebaseBarcode.driverLicense?.lastName, firebaseBarcode.driverLicense?.birthDate, firebaseBarcode.driverLicense?.expiryDate, firebaseBarcode.driverLicense?.gender, firebaseBarcode.driverLicense?.addressStreet, firebaseBarcode.driverLicense?.addressCity, firebaseBarcode.driverLicense?.addressState, firebaseBarcode.driverLicense?.addressZip))
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    Toast.makeText(baseContext, "Sorry, something went wrong!", Toast.LENGTH_SHORT).show()
                }
    }

    override fun onClick(v: View?) {
    }
}