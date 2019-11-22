package com.zebra.jamesswinton.rfidtestv2;

public class TagInfo {

    private String tagId;
    private boolean checked;

    public TagInfo(String tagId) {
        this.tagId = tagId;
        this.checked = true;
    }

    public TagInfo(String tagId, boolean checked) {
        this.tagId = tagId;
        this.checked = checked;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
