package com.example.pinviewdemo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.titanpinview.PinView

class MainActivity : AppCompatActivity(), PinView.OnPinEnteredListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pinView: PinView = findViewById(R.id.pinView)

        pinView.setOnPinEnteredListener(this)
    }

    override fun onPinEntered(pin: String) {
        Toast.makeText(this, pin, Toast.LENGTH_SHORT).show()
    }
}
