package com.example.android.storemanagement.create_product

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import android.content.Intent
import com.example.android.storemanagement.MainActivity.Companion.BARCODE_KEY


//Implementing the ResultHandler interface and overriding handleResult function

class BarcodeScanningActivity : AppCompatActivity(), ZBarScannerView.ResultHandler {

    private lateinit var scannerView: ZBarScannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerView = ZBarScannerView(this)
        setContentView(scannerView)
    }

    override fun onResume() {
        super.onResume()
        scannerView.setResultHandler(this)
        scannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        scannerView.stopCamera()
    }

    //* Barcode scanning result is displayed here.

    override fun handleResult(result: Result?) {
        Toast.makeText(this, result?.contents, Toast.LENGTH_SHORT).show()
        //Camera will stop after scanning result, so we need to resume the
        //preview in order scan more codes
        scannerView.resumeCameraPreview(this)

        val returnIntent = Intent()
        returnIntent.putExtra(BARCODE_KEY,result?.contents)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}