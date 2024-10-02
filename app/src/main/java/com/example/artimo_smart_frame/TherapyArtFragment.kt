package com.example.artimo_smart_frame

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.FocusHighlight
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter

class TherapyArtFragment : RowsSupportFragment() {
    // rootAdapter를 초기화하고, ListRowPresenter에 FocusHighlight를 설정
    private var rootAdapter: ArrayObjectAdapter = ArrayObjectAdapter(ListRowPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fragment의 어댑터를 rootAdapter로 설정
        adapter = rootAdapter
    }

    // 데이터를 바인딩하여 rowsAdapter에 추가하는 함수
    fun bindData(dataList: DataTherapyModel) {
             // 각 Result 객체에 대해 ArrayObjectAdapter 생성
        val arrayObjectAdapter = ArrayObjectAdapter(ItemTherapyPresenter())

        // 결과를 열 형태로 추가하기 위해 리스트
        dataList.result.forEach { result ->
            // Result 객체를 ArrayObjectAdapter에 추가
            arrayObjectAdapter.add(result)
        }

        // 결과의 제목으로 HeaderItem 생성
        val headerItem = HeaderItem(0, "명화 갤러리") // 필요에 따라 제목 변경

        // ListRow 생성
        val listRow = ListRow(headerItem, arrayObjectAdapter)

        // ListRow를 rootAdapter에 추가하여 화면에 표시
        rootAdapter.add(listRow)
    }
}
