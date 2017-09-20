/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.gsyvideoplayer.effect;

import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.GSYVideoGLView;

/**
 * 马赛克效果
 * Created by guoshuyu on 2017/09/20.
 */
public class PixelationEffect implements GSYVideoGLView.ShaderInterface {

    private float pixel = 40f;


    public PixelationEffect() {
    }

    /**
     * 1 - 100
     */
    public PixelationEffect(float pixel) {
        this.pixel = pixel;
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {

        String shader =  "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n"+
                "varying vec2 vTextureCoord;\n" +

                "float imageWidthFactor = "+ (1 / (float)mGlSurfaceView.getWidth()) +";\n" +
                "float imageHeightFactor = " + ( 1 /(float)mGlSurfaceView.getHeight()) + ";\n" +
                "uniform samplerExternalOES sTexture;\n" +
                "float pixel = " + pixel +";\n" +

                "void main()\n" +
                "{\n" +
                "  vec2 uv  = vTextureCoord.xy;\n" +
                "  float dx = pixel * imageWidthFactor;\n" +
                "  float dy = pixel * imageHeightFactor;\n" +
                "  vec2 coord = vec2(dx * floor(uv.x / dx), dy * floor(uv.y / dy));\n" +
                "  vec3 tc = texture2D(sTexture, coord).xyz;\n" +
                "  gl_FragColor = vec4(tc, 1.0);\n" +
                "}";

        return shader;

    }
}
