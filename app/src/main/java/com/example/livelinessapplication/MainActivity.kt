package com.example.livelinessapplication

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.example.livelinessapplication.databinding.ActivityMainBinding
import com.example.livelinessapplication.tasks.DetectionTask
import com.example.livelinessapplication.tasks.EyesBlinkDetectionTask

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraController: LifecycleCameraController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "Permission deny", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.launch(Manifest.permission.CAMERA)

    }

    private fun startCamera() {
        cameraController = LifecycleCameraController(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            FaceAnalyzer(buildLiveNessDetector())
        )
        cameraController.bindToLifecycle(this)
        binding.cameraPreview.controller = cameraController

        /* binding.cameraSwitch.setOnClickListener {
             cameraController.cameraSelector =
                 if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
         }*/
    }

    private fun buildLiveNessDetector(): LiveNessDetector {
        val listener = object : LiveNessDetector.Listener {
            @SuppressLint("SetTextI18n")
            override fun onTaskStarted(task: DetectionTask) {
                Toast.makeText(
                    this@MainActivity,
                    "Task started",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTaskCompleted(isLastTask: Boolean) {
                if (isLastTask) {
                    finishForResult()
                }
            }


            override fun onTaskFailed(task: DetectionTask, code: Int) {
                when (code) {
                    LiveNessDetector.ERROR_MULTI_FACES -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Please make sure there is only one face on the screen.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    LiveNessDetector.ERROR_NO_FACE -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Please make sure there is a face on the screen.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    LiveNessDetector.ERROR_OUT_OF_DETECTION_RECT -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Please make sure there is a face in the Rectangle.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            this@MainActivity,
                            "${task.taskName()} Failed.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onShowDetails(status: Boolean, message: String) {
                binding.livelinessResponseTextView.text = message
            }
        }
        val liveNessDetector = LiveNessDetector(EyesBlinkDetectionTask(), this@MainActivity)
        liveNessDetector.listener = listener
        return liveNessDetector
    }

    private fun finishForResult() {
        //   val result = ArrayList(imageFiles.takeLast(livenessDetector.getTaskSize()))
        //   setResult(RESULT_OK, Intent().putStringArrayListExtra(ResultContract.RESULT_KEY, result))
        finish()
    }
}