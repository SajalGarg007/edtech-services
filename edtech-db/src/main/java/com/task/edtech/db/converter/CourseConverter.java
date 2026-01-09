package com.task.edtech.db.converter;

import com.task.edtech.db.dto.CourseDTO;
import com.task.edtech.db.entity.Course;
import com.task.edtech.db.entity.User;
import com.task.edtech.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CourseConverter {

    private final UserRepository userRepository;

    public CourseDTO toDto(Course entity) {
        if (entity == null) return null;

        CourseDTO dto = new CourseDTO();
        dto.setId(entity.getInternalId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setMode(entity.getMode());
        dto.setAddress(entity.getAddress());
        dto.setPinCode(entity.getPinCode());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setScheduleInfo(entity.getScheduleInfo());
        dto.setPriceAmount(entity.getPriceAmount());
        dto.setIsFree(entity.getIsFree());
        dto.setCapacity(entity.getCapacity());
        dto.setIsPublished(entity.getIsPublished());

        return dto;
    }

    public Course toEntity(CourseDTO dto, Long userId) {
        if (dto == null) return null;

        Course entity = Course.builder()
                .internalId(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .mode(dto.getMode())
                .address(dto.getAddress())
                .pinCode(dto.getPinCode())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .scheduleInfo(dto.getScheduleInfo())
                .priceAmount(dto.getPriceAmount())
                .isFree(dto.getIsFree() != null ? dto.getIsFree() : false)
                .capacity(dto.getCapacity())
                .isPublished(Objects.nonNull(dto.getIsPublished()) ? dto.getIsPublished() : false)
                .build();

        if (userId != null) {
            userRepository.findById(userId)
                    .ifPresent(entity::setUser);
        }

        return entity;
    }
}

