package tw.com.ispan.eeit.model.entity.comment;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "report")
@Data
@NoArgsConstructor
public class ReportBean {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "report_type_id")
    private Integer reportTypeId;

    @Column(name = "comment_id")
    private Integer commentId;

    @Column(name = "submitter_id")
    private Integer submitterId;

    @Column(name = "submitter_type", length = 50)
    private String submitterType;

    @Column(length = 50)
    private String status;

    @Column(name = "repoart_date")
    private LocalDateTime reportDate;

    // 外鍵關係
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_type_id", insertable = false, updatable = false)
    private ReportTypeBean reportType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", insertable = false, updatable = false)
    private CommentBean comment;
}
