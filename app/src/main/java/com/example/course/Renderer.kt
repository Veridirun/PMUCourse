package com.example.course

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Renderer(private val context: Context) : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mVPMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val lightPos = floatArrayOf(1f, 1f, 3f)

    private lateinit var table: Table
    private lateinit var glass: Glass
    private lateinit var apple: Sphere
    private lateinit var pumpkin: Sphere
    private lateinit var orange: Sphere
    private lateinit var cucumber: Sphere
    private lateinit var banana: Banana

    private var appleTexture: Int = 0
    private var pumpkinTexture: Int = 0
    private var orangeTexture: Int = 0
    private var cucumberTexture: Int = 0
    private var bananaTexture: Int = 0

    override fun onSurfaceCreated(arg0: GL10?, arg1: EGLConfig?) {
        GLES20.glClearColor(0.53f, 0.81f, 0.92f, 1f)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LESS)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        table = Table(context)

        banana = Banana(context)
        bananaTexture = loadTexture(R.drawable.banana)

        apple = Sphere(radius = 0.3f/3)
        appleTexture = loadTexture(R.drawable.apple)

        glass = Glass(context)

        pumpkin = Sphere(radius = 0.8f/3, scaleX = 1.5f, scaleY = 1.0f, scaleZ = 1.0f)
        pumpkinTexture = loadTexture(R.drawable.pumpkin)

        orange = Sphere(radius = 0.35f/3)
        orangeTexture = loadTexture(R.drawable.orange)

        cucumber = Sphere(radius = 0.25f/3, scaleX = 3f,scaleY = 1.0f,scaleZ = 1.0f)
        cucumberTexture = loadTexture(R.drawable.cucumber)
    }

    override fun onDrawFrame(arg0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        //установление единичной матрицы (дефолт позиция, масштаб, поворот)
        Matrix.setIdentityM(modelMatrix, 0)
        //точка обзора
        Matrix.setLookAtM(viewMatrix, 0,
            1f, 1f, 5f,
            0f, 0f, 3.2f,
            0f, 1f, 0f)
        //Стол
        //преобразование из 3D в 2D
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        //инверсия, чтоб правильно вычислить нормали
        Matrix.invertM(normalMatrix, 0, viewMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, -1f, 0f)
        Matrix.scaleM(modelMatrix, 0, 2f, 2f, 2f)
        Matrix.rotateM(modelMatrix, 0, 0f, 0.1f, 0.1f, 0f)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        Matrix.setIdentityM(modelMatrix, 0)
        table.draw(mVPMatrix)

        //яблоко
        Matrix.setIdentityM(modelMatrix, 0)
        //Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 5f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        //Matrix.invertM(normalMatrix, 0, viewMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -0.5f, -0.6f, 2.5f)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        apple.draw(mVPMatrix, normalMatrix, lightPos, viewMatrix, appleTexture)

        // тыква
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.6f, -0.5f, 2.7f)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        pumpkin.draw(mVPMatrix, normalMatrix, lightPos, viewMatrix, pumpkinTexture)

        // огурец
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -0.1f, -0.6f, 3.2f)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        cucumber.draw(mVPMatrix, normalMatrix, lightPos, viewMatrix, cucumberTexture)

        // Апельсин
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -0.1f, -0.5f, 3f)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        orange.draw(mVPMatrix, normalMatrix, lightPos, viewMatrix, orangeTexture)

        // Стакан
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -0.7f, -0.55f, 3f)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        glass.draw(mVPMatrix, lightPos, viewMatrix)

        // Банан
        Matrix.setIdentityM(modelMatrix, 0)
        //Matrix.rotateM(modelMatrix, 0, 70f, 0f, 1f, 0f)
        Matrix.translateM(modelMatrix, 0, -1f, -0.5f, 3.5f)
        Matrix.scaleM(modelMatrix, 0, 2f, 2f, 2f)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        banana.draw(mVPMatrix, normalMatrix, lightPos, viewMatrix, bananaTexture)
    }

    override fun onSurfaceChanged(arg0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 10f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 5f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    private fun loadTexture(resourceId: Int): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        val textureId = textureIds[0]

        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        bitmap.recycle()

        return textureId
    }
}