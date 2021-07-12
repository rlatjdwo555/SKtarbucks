package local;

public class Canceled extends AbstractEvent {

    private Long cafeId;
    private String chkDate;
    private String cafeNm;
    private Long pCnt;

    public Long getCafeId() {
        return cafeId;
    }

    public void setCafeId(Long cafeId) {
        this.cafeId = cafeId;
    }
    public String getChkDate() {
        return chkDate;
    }

    public void setChkDate(String chkDate) {
        this.chkDate = chkDate;
    }

    public String getCafeNm() {
        return cafeNm;
    }

    public void setCafeNm(String cafeNm) {
        this.cafeNm = cafeNm;
    }

    public Long getpCnt() {
        return pCnt;
    }

    public void setpCnt(Long pCnt) {
        this.pCnt = pCnt;
    }

}