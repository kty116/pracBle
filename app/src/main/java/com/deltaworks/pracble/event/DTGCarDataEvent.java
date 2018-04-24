package com.deltaworks.pracble.event;


import com.deltaworks.pracble.model.DTGInfo;

/**
 * 화면에 데이터 표시하고 싶을때 보내는 이벤트
 */

public class DTGCarDataEvent implements Event {
    private DTGInfo dtgInfo;

    public DTGCarDataEvent(DTGInfo dtgInfo) {
        this.dtgInfo = dtgInfo;
    }

    public DTGInfo getDtgInfo() {
        return dtgInfo;
    }

    public void setDtgInfo(DTGInfo dtgInfo) {
        this.dtgInfo = dtgInfo;
    }

    @Override
    public String toString() {
        return "DTGCarDataEvent{" +
                "dtgInfo=" + dtgInfo +
                '}';
    }
}
