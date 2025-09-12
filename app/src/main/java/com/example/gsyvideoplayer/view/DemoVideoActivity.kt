package com.example.gsyvideoplayer.view

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gsyvideoplayer.VideoAdapter
import com.example.gsyvideoplayer.TabBean
import com.example.gsyvideoplayer.BaseResponse
import com.example.gsyvideoplayer.CustomScrollListener
import com.example.gsyvideoplayer.GetJsonDataUtil
import com.example.gsyvideoplayer.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DemoVideoActivity : AppCompatActivity() {

    private val mAiVideoAdapter by lazy { VideoAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_demo_video)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            delay(1000)
            initRecycler()
        }
    }


    private fun initRecycler() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setItemViewCacheSize(30)
        mAiVideoAdapter.setList(getData())
        recyclerView.adapter = mAiVideoAdapter
        recyclerView.addOnScrollListener(CustomScrollListener(0.85F, true, linearLayoutManager, { recyclerView, dx, dy ->

        }, {
            Log.e("视频外层", "新增:${it}")
            mAiVideoAdapter.addNewVideo(it)
        }, {
            mAiVideoAdapter.removeVideo(it)
            Log.e("视频外层", "移除:${it}")
        }, {
            Log.e("视频外层", "所有可见:${it}")
        }))
    }


    /**
     * 保存列表对象
     */
    fun getData(): MutableList<TabBean> {
        val getJsonDataUtil = GetJsonDataUtil.getJson(this, "Test.json")
        val gson = Gson()
        //解决 泛型<T>不能强转为List
        val list: BaseResponse<MutableList<TabBean>> = gson.fromJson(getJsonDataUtil, object : TypeToken<BaseResponse<MutableList<TabBean>>>() {}.type)
        return list.data ?: mutableListOf()
    }

}
