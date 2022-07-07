package org.sjtu.router;

/**
 * @author hcstart
 * @create 2022-06-17 23:02
 */
public enum SelectorEnum {

    RANDOM_SELECTOR(0, "random");

    int code;
    String desc;

    SelectorEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
