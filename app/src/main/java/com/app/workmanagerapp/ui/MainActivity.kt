package com.app.workmanagerapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.workmanagerapp.R
import com.app.workmanagerapp.databinding.ActivityMainBinding
import com.google.firebase.messaging.FirebaseMessaging
import com.prime.track.MainClass

import com.prime.track.utils.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var periodicHelper: PeriodicHelper
    private lateinit var mainClass: MainClass


    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainClass = MainClass(this)
        mainClass.initializeValue()
        periodicHelper = PeriodicHelper(this)
        if (prefStorage.isWorkStart) {
            binding.btnStart.isVisible = false
            binding.btnStop.isVisible = true
        } else {
            binding.btnStart.isVisible = true
            binding.btnStop.isVisible = false
        }

        binding.btnStart.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pm: PowerManager =
                    getSystemService(POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    showRationaleDialog()
                } else {
                    getToken()
                    prefStorage.isWorkStart = true
                    binding.btnStart.isVisible = false
                    binding.btnStop.isVisible = true
                    mainClass.doTask(true)

                }

            } else {
                getToken()
                prefStorage.isWorkStart = true
                binding.btnStart.isVisible = false
                binding.btnStop.isVisible = true
                mainClass.doTask(true)

            }
        }
        binding.btnStop.setOnClickListener {
            periodicHelper.stopLog()

            ApiTask().sendToken(
                this, prefStorage.firebaseToken,
                NOTIFICATION_STOP
            ) {
                prefStorage.isWorkStart = false
                binding.btnStop.isVisible = false
                binding.btnStart.isVisible = true
            }

        }


    }

    override fun onResume() {
        super.onResume()
        showRationaleDialog()
    }

    private var dialog: AlertDialog? = null
    private fun showRationaleDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm: PowerManager =
                getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.app_name))
                    .setMessage("To check background service app need some permissions.")
                    .setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                        batteryOptimize()

                    }
                dialog = builder.create()
                dialog?.show()
            }
        }
    }


    @SuppressLint("BatteryLife")
    private fun batteryOptimize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val mIntent = Intent()
            val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                mIntent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                mIntent.data = Uri.parse("package:$packageName")
                activityResultLauncher.launch(mIntent)
            }
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when {
                result.resultCode == Activity.RESULT_OK && result.data != null -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager

                        if (pm.isIgnoringBatteryOptimizations(packageName)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                            }
                        } else {
                            showRationaleDialog()
                        }
                    }
                }
            }
        }

    fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            } else {
                prefStorage.firebaseToken = task.result!!
                ApiTask().sendToken(
                    this, task.result!!,
                    NOTIFICATION_START
                ) {}
            }
        }
    }

}