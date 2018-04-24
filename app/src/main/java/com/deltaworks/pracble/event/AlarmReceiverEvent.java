package com.deltaworks.pracble.event;

/**
 * 알람브로드캐스트리시버 콜백이 오면 보내는 이벤트
 */

public class AlarmReceiverEvent implements Event {
    private String actionType;

    public AlarmReceiverEvent(String actionType) {
        this.actionType = actionType;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}
