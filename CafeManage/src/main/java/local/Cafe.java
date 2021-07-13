package local;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Cafe_table")
public class Cafe {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String cafeNm;
    private Long pCnt;
    private String chkDate;

    @PostPersist
    public void onPostPersist(){
        CafeRegistered cafeRegistered = new CafeRegistered();
        BeanUtils.copyProperties(this, cafeRegistered);
        cafeRegistered.publishAfterCommit();
    }

    @PreUpdate
    public  void onPreUpdate(){
        /*
        강제적 Delay
        try {
            Thread.currentThread().sleep((long)10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
    }

    @PostUpdate
    public void onPostUpdate(){

        CafeChanged cafeChanged = new CafeChanged();
        BeanUtils.copyProperties(this, cafeChanged);
        cafeChanged.publishAfterCommit();
    }


    @PreRemove
    public void onPreRemove(){
        CafeDeleted cafeDeleted = new CafeDeleted();
        BeanUtils.copyProperties(this, cafeDeleted);
        cafeDeleted.publishAfterCommit();

    }


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
