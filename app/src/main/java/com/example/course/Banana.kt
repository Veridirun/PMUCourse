package com.example.course

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

fun loadObj(context: Context, resourceId: Int): ModelData {
    val vertices = mutableListOf<Float>()
    val textureCoords = mutableListOf<Float>()
    val normals = mutableListOf<Float>()
    val indices = mutableListOf<Short>()

    val inputStream = context.resources.openRawResource(resourceId)
    val reader = BufferedReader(InputStreamReader(inputStream))

    reader.forEachLine { line ->
        val parts = line.split(" ")
        when (parts[0]) {
            "v" -> { // Вершины
                vertices.add(parts[1].toFloat())
                vertices.add(parts[2].toFloat())
                vertices.add(parts[3].toFloat())
            }
            "vt" -> { // Текстурные координаты
                textureCoords.add(parts[1].toFloat())
                textureCoords.add(parts[2].toFloat())
            }
            "vn" -> { // Нормали
                normals.add(parts[1].toFloat())
                normals.add(parts[2].toFloat())
                normals.add(parts[3].toFloat())
            }
            "f" -> { // Индексы
                parts.drop(1).forEach { vertex ->
                    val vertexParts = vertex.split("/")
                    indices.add((vertexParts[0].toInt() - 1).toShort()) // Индекс вершины
                }
            }
        }
    }

    return ModelData(
        vertices = vertices.toFloatArray(),
        indices = indices.toShortArray(),
        textureCoords = textureCoords.toFloatArray(),
        normals = normals.toFloatArray()
    )
}

data class ModelData(
    val vertices: FloatArray,
    val indices: ShortArray,
    val textureCoords: FloatArray,
    val normals: FloatArray
)

class Banana(
    public val context: Context
) {
    private lateinit var shaderProgram: ShaderProgram
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: ShortBuffer
    private lateinit var textureBuffer: FloatBuffer
    private lateinit var normalBuffer: FloatBuffer
    private lateinit var vertices: FloatArray
    private lateinit var indices: ShortArray
    private lateinit var textureCoords: FloatArray
    private lateinit var normals: FloatArray

    init {

        shaderProgram = ShaderProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE)
        var objData = loadObj(context, R.raw.banana)
        vertices = objData.vertices
        indices = objData.indices
        textureCoords = objData.textureCoords
        normals = objData.normals
        setupBuffers()
    }

    private fun setupBuffers() {
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(indices)
                position(0)
            }
        }

        textureBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(textureCoords)
                position(0)
            }
        }

        normalBuffer = ByteBuffer.allocateDirect(normals.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(normals)
                position(0)
            }
        }
    }

    fun draw(
        mvpMatrix: FloatArray,
        normalMatrix: FloatArray,
        lightPos: FloatArray,
        viewPos: FloatArray,
        textureId: Int
    ) {
        shaderProgram.use()

        val positionHandle = GLES20.glGetAttribLocation(shaderProgram.programId, "a_Position")
        val texCoordHandle = GLES20.glGetAttribLocation(shaderProgram.programId, "a_TexCoord")
        val normalHandle = GLES20.glGetAttribLocation(shaderProgram.programId, "a_Normal")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram.programId, "u_MVPMatrix")
        val normalMatrixHandle = GLES20.glGetUniformLocation(shaderProgram.programId, "u_NormalMatrix")
        val lightPosHandle = GLES20.glGetUniformLocation(shaderProgram.programId, "u_LightPos")
        val viewPosHandle = GLES20.glGetUniformLocation(shaderProgram.programId, "u_ViewPos")

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(normalMatrixHandle, 1, false, normalMatrix, 0)
        GLES20.glUniform3fv(lightPosHandle, 1, lightPos, 0)
        GLES20.glUniform3fv(viewPosHandle, 1, viewPos, 0)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glEnableVertexAttribArray(normalHandle)

        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indices.size,
            GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
    }

    companion object {
        private const val VERTEX_SHADER_CODE = """
            attribute vec4 a_Position;
            attribute vec2 a_TexCoord;
            attribute vec3 a_Normal;
            uniform mat4 u_MVPMatrix;
            uniform mat4 u_NormalMatrix;
            uniform vec3 u_LightPos;
            uniform vec3 u_ViewPos;
            varying vec2 v_TexCoord;
            varying vec3 v_Normal;
            varying vec3 v_LightDir;
            varying vec3 v_ViewDir;

            void main() {
                gl_Position = u_MVPMatrix * a_Position;

                // Transform the normal to eye space
                v_Normal = normalize(vec3(u_NormalMatrix * vec4(a_Normal, 0.0)));

                // Compute light direction and view direction
                v_LightDir = normalize(u_LightPos - vec3(gl_Position));
                v_ViewDir = normalize(u_ViewPos - vec3(gl_Position));

                v_TexCoord = a_TexCoord;
            }
        """

        private const val FRAGMENT_SHADER_CODE = """
            precision mediump float;
            varying vec2 v_TexCoord;
            varying vec3 v_Normal;
            varying vec3 v_LightDir;
            varying vec3 v_ViewDir;
            uniform sampler2D u_Texture;

            void main() {
                vec4 texColor = texture2D(u_Texture, v_TexCoord);
                
                // Normalize the normal vector
                vec3 norm = normalize(v_Normal);
                
                // Compute the diffuse and specular lighting
                float diff = max(dot(norm, v_LightDir), 0.0);
                vec3 reflectDir = reflect(-v_LightDir, norm);
                float spec = pow(max(dot(v_ViewDir, reflectDir), 0.0), 32.0); // Shininess factor

                // Combine the color and lighting
                vec3 ambient = vec3(0.1) * texColor.rgb; // Ambient light
                vec3 diffuse = diff * texColor.rgb; // Diffuse light
                vec3 specular = spec * vec3(1.0); // Specular light color (white)

                vec3 finalColor = ambient + diffuse + specular;
                gl_FragColor = vec4(finalColor, texColor.a);
            }
        """
    }
}
