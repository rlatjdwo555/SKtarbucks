package local;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@RestController
public class CafeController {

    @Autowired
    CafeRepository cafeRepository;


    @PostMapping("/cafes")
    void cafeInsert(@RequestBody Cafe data) {
        cafeRepository.save(data);
    }

    @PutMapping("/cafes/{cafeId}")
    void decreasePcnt(@PathVariable(value = "cafeId") Long cafeId) {

        Optional<Cafe> a = cafeRepository.findById(cafeId);
        if (a.isPresent()) {
            Cafe b = a.get();
            b.setPCnt(b.getPCnt() - 1);
            cafeRepository.save(b);
        }
    }


    @GetMapping("/cafes")
    Iterable<Cafe> getCafeList() {
        Iterable<Cafe> result = cafeRepository.findAll();
        return result;
    }

    @GetMapping("/cafes/{cafeId}")
    Cafe getCafeById(@PathVariable(value = "cafeId") Long cafeId) {
        System.out.println("productStockCheck call");
        Optional<Cafe> a = cafeRepository.findById(cafeId);
        return a.get();
    }


    @DeleteMapping("/cafes/{cafeId}")
    void cafeDelete(@PathVariable(value = "cafeId") Long cafeId) {
        cafeRepository.deleteById(cafeId);

    }

}
