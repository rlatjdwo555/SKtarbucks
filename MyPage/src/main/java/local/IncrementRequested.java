
package local;

public class IncrementRequested extends AbstractEvent {

    private String cafeId;
    private String cafeNm;
    private String status;

    public String getCafeId() {
        return cafeId;
    }

    public void setCafeId(String cafeId) {
        this.cafeId = cafeId;
    }

    public String getCafeNm() {
        return cafeNm;
    }

    public void setCafeNm(String cafeNm) {
        this.cafeNm = cafeNm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
