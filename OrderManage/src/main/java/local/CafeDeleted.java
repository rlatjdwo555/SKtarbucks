
package local;

public class CafeDeleted extends AbstractEvent {

    private Long id;
    private String cafeNm;
    private Long pCnt;
    private String chkDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getCafeNm() {
        return cafeNm;
    }

    public void setCafeNm(String cafeNm) {
        this.cafeNm = cafeNm;
    }
    public Long getPCnt() {
        return pCnt;
    }

    public void setPCnt(Long pCnt) {
        this.pCnt = pCnt;
    }
    public String getChkDate() {
        return chkDate;
    }

    public void setChkDate(String chkDate) {
        this.chkDate = chkDate;
    }
}
