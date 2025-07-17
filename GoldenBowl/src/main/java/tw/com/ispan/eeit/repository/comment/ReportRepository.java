package tw.com.ispan.eeit.repository.comment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tw.com.ispan.eeit.model.entity.comment.ReportBean;

public interface ReportRepository extends JpaRepository<ReportBean, Integer> {
    @EntityGraph(attributePaths = { "reportType", "comment" })
    List<ReportBean> findAll();

    @EntityGraph(attributePaths = { "reportType", "comment" })
    Optional<ReportBean> findById(Integer id);

    // 使用 JOIN FETCH 預先加載 reportType 和 comment 關聯 0716 JIMMY新增
    @Query("SELECT r FROM ReportBean r JOIN FETCH r.reportType JOIN FETCH r.comment")
    List<ReportBean> findAllWithDetails();

    // 0716 JIMMY新增
    @Query("SELECT r FROM ReportBean r JOIN FETCH r.reportType JOIN FETCH r.comment WHERE r.id = :id")
    Optional<ReportBean> findByIdWithDetails(@Param("id") Integer id);

}
