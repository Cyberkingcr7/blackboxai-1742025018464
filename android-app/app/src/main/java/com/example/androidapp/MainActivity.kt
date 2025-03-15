package com.example.androidapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.example.androidapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.BODY_SENSORS
    )

    private val permissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            showSuccessMessage()
        } else {
            handleDeniedPermissions(permissions)
        }
        updatePermissionStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        updatePermissionStatus()
    }

    private fun setupUI() {
        binding.requestPermissionsButton.setOnClickListener {
            requestPermissions()
        }

        binding.openSettingsButton.setOnClickListener {
            openAppSettings()
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isEmpty()) {
            showSuccessMessage()
            return
        }

        if (shouldShowRequestRationaleDialog(permissionsToRequest)) {
            showPermissionRationaleDialog(permissionsToRequest)
        } else {
            permissionRequestLauncher.launch(permissionsToRequest)
        }
    }

    private fun shouldShowRequestRationaleDialog(permissions: Array<String>): Boolean {
        return permissions.any { shouldShowRequestPermissionRationale(it) }
    }

    private fun showPermissionRationaleDialog(permissions: Array<String>) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.permission_rationale_title))
            .setMessage(getString(R.string.permission_rationale_message))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                permissionRequestLauncher.launch(permissions)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun handleDeniedPermissions(permissions: Map<String, Boolean>) {
        val permanentlyDenied = permissions.filter { !it.value }
            .any { !shouldShowRequestPermissionRationale(it.key) }

        if (permanentlyDenied) {
            showSettingsSnackbar()
        } else {
            showDeniedMessage()
        }
    }

    private fun updatePermissionStatus() {
        val status = StringBuilder()
        permissions.forEach { permission ->
            val isGranted = ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED

            val permissionName = permission.split(".").last()
            status.append("$permissionName: ${if (isGranted) "✓" else "✗"}\n")
        }
        binding.permissionStatusTextView.text = status.toString()
    }

    private fun showSuccessMessage() {
        Snackbar.make(
            binding.root,
            getString(R.string.permissions_granted),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun showDeniedMessage() {
        Snackbar.make(
            binding.root,
            getString(R.string.permissions_denied),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun showSettingsSnackbar() {
        Snackbar.make(
            binding.root,
            getString(R.string.permissions_denied_permanently),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(R.string.settings)) {
            openAppSettings()
        }.show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    }
}
