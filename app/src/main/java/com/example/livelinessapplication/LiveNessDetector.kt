package com.example.livelinessapplication

import android.content.Context
import android.os.Build
import android.widget.Toast
import com.google.mlkit.vision.face.Face
import com.example.livelinessapplication.tasks.DetectionTask
import com.example.livelinessapplication.utils.DetectionUtils
import java.util.Deque
import java.util.LinkedList
import kotlin.math.max

class LiveNessDetector(private val detectionTask: DetectionTask, private val context: Context) {

    private var faceDetected = false
    var listener : Listener? = null

    companion object {
        private const val FACE_CACHE_SIZE = 5
        private const val NO_ERROR = -1
        const val ERROR_NO_FACE = 0
        const val ERROR_MULTI_FACES = 1
        const val ERROR_OUT_OF_DETECTION_RECT = 2
    }

    private var currentErrorState = NO_ERROR
    private val lastFaces: Deque<Face> = LinkedList()

    fun process(faces: List<Face>?, detectionSize: Int, timestamp: Long) {
        if (faces.isNullOrEmpty()) {
            changeErrorState(ERROR_NO_FACE)
            return
        }
        if (!faces.isNullOrEmpty() ) {

            val face = faces.first()
            val faceSize = max(face.boundingBox.width(), face.boundingBox.height())

            if(!faceDetected){
                faceDetected = true
                listener?.onShowDetails(true, "Face detected successfully")
            }
            if (faceSize < detectionSize * 0.3) {
                listener?.onShowDetails(true, "Please get closer to the camera.")
            }

        }

        if (faces.size > 1) {
            changeErrorState(ERROR_MULTI_FACES)
            return
        }

        val face = faces.first()
        if (!DetectionUtils.isFaceInDetectionRect(face, detectionSize)) {
            changeErrorState(ERROR_OUT_OF_DETECTION_RECT)
            return
        }

        // Cache current face
        lastFaces.offerFirst(face)
        if (lastFaces.size > FACE_CACHE_SIZE) {
            lastFaces.pollLast()
        }

        // Process the face for the single detection task
        if (detectionTask.process(face, timestamp)) {
            // Task completed
            currentErrorState = NO_ERROR
            listener?.onShowDetails(true, "EyesBlinkDetection task completed")
        }
    }

    private fun changeErrorState(newErrorState: Int) {
        if (newErrorState != currentErrorState) {
            currentErrorState = newErrorState
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    interface Listener {

        fun onTaskStarted(task: DetectionTask)

        fun onTaskCompleted( isLastTask: Boolean)

        fun onTaskFailed(task: DetectionTask, code: Int)

        fun onShowDetails(status : Boolean, message : String)
    }
}
