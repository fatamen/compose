package tw.com.ispan.eeit.service.food;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.entity.food.TagBean;
import tw.com.ispan.eeit.repository.food.TagRepository;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public List<TagBean> getAllTags() {
        return tagRepository.findAll();
    }

    public Optional<TagBean> getTagById(Integer id) {
        return tagRepository.findById(id);
    }

    public TagBean createTag(TagBean tag) {
        return tagRepository.save(tag);
    }

    public TagBean updateTag(Integer id, TagBean tagDetails) {
        Optional<TagBean> optionalTag = tagRepository.findById(id);
        if (optionalTag.isPresent()) {
            TagBean existingTag = optionalTag.get();
            existingTag.setName(tagDetails.getName());
            // 注意：對於 ManyToMany 和 OneToMany 關聯，直接在 update 方法中設置可能需要更複雜的邏輯
            // existingTag.setFoods(tagDetails.getFoods());
            // existingTag.setUserTags(tagDetails.getUserTags());
            return tagRepository.save(existingTag);
        }
        return null; // 或者拋出一個自定義異常
    }

    public boolean deleteTag(Integer id) {
        if (tagRepository.existsById(id)) {
            tagRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
