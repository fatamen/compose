package tw.com.ispan.eeit.controller.food;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.dto.food.TagDTO;
import tw.com.ispan.eeit.model.entity.food.TagBean;
import tw.com.ispan.eeit.service.food.TagService;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<TagBean> tags = tagService.getAllTags();
        // 將 TagBean 列表轉換為 TagDTO 列表
        List<TagDTO> tagDTOs = tags.stream()
                .map(bean -> {
                    TagDTO dto = new TagDTO();
                    BeanUtils.copyProperties(bean, dto); // 複製 id 和 name
                    return dto;
                })
                .collect(Collectors.toList());
        return new ResponseEntity<>(tagDTOs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Integer id) {
        Optional<TagBean> tag = tagService.getTagById(id);
        return tag.map(bean -> {
            TagDTO dto = new TagDTO();
            BeanUtils.copyProperties(bean, dto);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<TagDTO> createTag(@RequestBody TagDTO tagDto) {
        TagBean tagBean = new TagBean();
        BeanUtils.copyProperties(tagDto, tagBean); // 將 DTO 屬性複製到 Bean

        TagBean createdTag = tagService.createTag(tagBean);

        TagDTO responseDto = new TagDTO();
        BeanUtils.copyProperties(createdTag, responseDto); // 將創建的 Bean 屬性複製回 DTO
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    // 更新標籤 (使用 DTO 作為請求體)
    @PutMapping("/{id}")
    public ResponseEntity<TagDTO> updateTag(@PathVariable Integer id, @RequestBody TagDTO tagDto) {
        // 從 DTO 轉換為 Bean
        TagBean tagBean = new TagBean();
        // 注意：這裡只會複製 DTO 中有的屬性 (id, name) 到 tagBean
        // 如果你的 Service 層需要使用 Bean 的其他屬性來更新，需要額外從資料庫載入
        BeanUtils.copyProperties(tagDto, tagBean);

        TagBean updatedTag = tagService.updateTag(id, tagBean); // 假設 Service 會處理更新邏輯

        if (updatedTag != null) {
            // 從更新後的 Bean 轉換回 DTO 以返回
            TagDTO responseDto = new TagDTO();
            BeanUtils.copyProperties(updatedTag, responseDto); // Bean -> DTO
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTag(@PathVariable Integer id) {
        if (tagService.deleteTag(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
