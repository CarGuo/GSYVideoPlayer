package com.shuyu.gsyvideoplayer.effect;

import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.GSYVideoGLView.ShaderInterface;

/**
 * 高斯模糊
 * Created by guoshuyu on 2017/9/17.
 */
public class GaussianBlurEffect implements ShaderInterface {
    public final static int TYPEX = 1;
    public final static int TYPEY = 2;
    public final static int TYPEXY = 3;

    private float radius = 1.0f;
    private String blurTypeString = "vec2(1.0,0.0)";

    /**
     * Initialize Effect
     */
    public GaussianBlurEffect(float radius) {
        this.radius = radius;
    }

    public GaussianBlurEffect(float radius, int blurType) {
        this.radius = radius;
        switch (blurType) {
            case TYPEX:
                blurTypeString = "vec2(1.0,0.0)";
                break;
            case TYPEY:
                blurTypeString = "vec2(0.0,1.0)";
                break;
            case TYPEXY:
                blurTypeString = "vec2(1.0,1.0)";
                break;
        }
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {

        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying vec2 vTextureCoord;\n" +
                "uniform samplerExternalOES sTexture;\n" +
                "const float resolution=1024.0;\n" +
                "const float radius = " + radius + ";\n" +
                "vec2 dir =" + blurTypeString + "; //若为x模糊，可传入（1.0,0.0）  y模糊  （0.0,1.0）\n" +
                "\n" +
                "void main() {\n" +
                "    //this will be our RGBA sum\n" +
                "    vec4 sum = vec4(0.0);\n" +
                "    \n" +
                "    //our original texcoord for this fragment\n" +
                "    vec2 tc = vTextureCoord;\n" +
                "    \n" +
                "    //the amount to blur, i.e. how far off center to sample from \n" +
                "    //1.0 -> blur by one pixel\n" +
                "    //2.0 -> blur by two pixels, etc.\n" +
                "    float blur = radius/resolution; \n" +
                "    \n" +
                "    //the direction of our blur\n" +
                "    //(1.0, 0.0) -> x-axis blur\n" +
                "    //(0.0, 1.0) -> y-axis blur\n" +
                "    float hstep = dir.x;\n" +
                "    float vstep = dir.y;\n" +
                "    \n" +
                "    \n" +
                "    //apply blurring, using a 9-tap filter with predefined gaussian weights\n" +
                "    \n" +
                "    sum += texture2D(sTexture, vec2(tc.x - 4.0*blur*hstep, tc.y - 4.0*blur*vstep)) * 0.0162162162;\n" +
                "    sum += texture2D(sTexture, vec2(tc.x - 3.0*blur*hstep, tc.y - 3.0*blur*vstep)) * 0.0540540541;\n" +
                "    sum += texture2D(sTexture, vec2(tc.x - 2.0*blur*hstep, tc.y - 2.0*blur*vstep)) * 0.1216216216;\n" +
                "    sum += texture2D(sTexture, vec2(tc.x - 1.0*blur*hstep, tc.y - 1.0*blur*vstep)) * 0.1945945946;\n" +
                "    \n" +
                "    sum += texture2D(sTexture, vec2(tc.x, tc.y)) * 0.2270270270;\n" +
                "    \n" +
                "    sum += texture2D(sTexture, vec2(tc.x + 1.0*blur*hstep, tc.y + 1.0*blur*vstep)) * 0.1945945946;\n" +
                "    sum += texture2D(sTexture, vec2(tc.x + 2.0*blur*hstep, tc.y + 2.0*blur*vstep)) * 0.1216216216;\n" +
                "    sum += texture2D(sTexture, vec2(tc.x + 3.0*blur*hstep, tc.y + 3.0*blur*vstep)) * 0.0540540541;\n" +
                "    sum += texture2D(sTexture, vec2(tc.x + 4.0*blur*hstep, tc.y + 4.0*blur*vstep)) * 0.0162162162;\n" +
                "\n" +
                "    vec4 cc= texture2D(sTexture,vTextureCoord );\n" +
                "\n" +
                "    //discard alpha for our simple demo, multiply by vertex color and return\n" +
                "    gl_FragColor =vec4(sum.rgb, cc.a);\n" +
                "}";
    }
}
