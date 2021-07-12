
package local;

public class RequestCompleted extends AbstractEvent {

    private Long id;
    private Long cafeId;
    private String cafeNm;
    private String custNm;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCafeId() {
        return cafeId;
    }

    public void setCafeId(Long cafeId) {
        this.cafeId = cafeId;
    }
    public String getCafeNm() {
        return cafeNm;
    }

    public void setCafeNm(String cafeNm) {
        this.cafeNm = cafeNm;
    }
    public String getCustNm() {
        return custNm;
    }

    public void setCustNm(String custNm) {
        this.custNm = custNm;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
