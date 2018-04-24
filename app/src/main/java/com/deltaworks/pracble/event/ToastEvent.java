package com.deltaworks.pracble.event;

/**
 * 서비스에서 토스트 보내고 싶을때 사용하는 이벤트
 */

public class ToastEvent implements Event {
    private String text;

    public ToastEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
