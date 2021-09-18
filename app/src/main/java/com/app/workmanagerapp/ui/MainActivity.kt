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
import androidx.work.WorkManager
import com.app.workmanagerapp.R
import com.app.workmanagerapp.databinding.ActivityMainBinding
import com.app.workmanagerapp.modules.Scopes
import com.app.workmanagerapp.storages.PrefStorage
import com.app.workmanagerapp.utils.*
import toothpick.Toothpick


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var periodicHelper: PeriodicHelper
    private val prefStorage: PrefStorage =
        Toothpick.openScope(Scopes.APP).getInstance(PrefStorage::class.java)

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        periodicHelper = PeriodicHelper(this)
        if (prefStorage.isWorkStart) {
            binding.btnStart.isVisible = false
            binding.btnStop.isVisible = true
        }else{
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        permissions(
                            Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) { allGranted, _, _ ->
                            if (allGranted) {
                                prefStorage.isWorkStart = true
                                binding.btnStart.isVisible = false
                                binding.btnStop.isVisible = true
                                getLocation(false)
                                periodicHelper.getToken()
                                periodicHelper.startLog()

                            }
                        }
                    } else {
                        permissions(
                            Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) { allGranted, _, _ ->
                            if (allGranted) {
                                getLocation(false)
                                prefStorage.isWorkStart = true
                                binding.btnStart.isVisible = false
                                binding.btnStop.isVisible = true
                                periodicHelper.getToken()
                                periodicHelper.startLog()

                            }
                        }
                    }
                }

            } else {
                getLocation(false)
                prefStorage.isWorkStart = true
                binding.btnStart.isVisible = false
                binding.btnStop.isVisible = true
                periodicHelper.getToken()
                periodicHelper.startLog()
            }
        }
        binding.btnStop.setOnClickListener {
            WorkManager.getInstance().cancelAllWork()

            TokenSendTask().sendToken(this, prefStorage.firebaseToken,
                NOTIFICATION_STOP
            ){
                getLocation(true)
                prefStorage.isWorkStart = false
                binding.btnStop.isVisible = false
                binding.btnStart.isVisible = true
            }

        }

        binding.btnScan.setOnClickListener {
            wifiStatus { _, _ ->

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
                                permissions(
                                    Manifest.permission.READ_CALL_LOG,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                ) { _, _, _ ->
                                }
                            } else {
                                permissions(
                                    Manifest.permission.READ_CALL_LOG,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) { _, _, _ ->

                                }
                            }
                        } else {
                            showRationaleDialog()
                        }
                    }
                }
            }
        }


}