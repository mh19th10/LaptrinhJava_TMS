package vn.edu.uth.quanlidaythem.dto;

import java.util.List;
import java.util.stream.Collectors;

import vn.edu.uth.quanlidaythem.model.ClassEntity;
import vn.edu.uth.quanlidaythem.model.ScheduleEntity;
import vn.edu.uth.quanlidaythem.model.StudentEntity;
import vn.edu.uth.quanlidaythem.model.TeacherEntity;
/**
 * Mapper utility để convert Entity ↔ DTO
 * Giúp tránh circular references khi serialize JSON
 */
public class DTOMapper {

    /**
     * Convert ClassEntity → ClassDTO
     * Tự động convert nested TeacherEntity, ScheduleEntity, StudentEntity thành DTO
     */
    public static ClassDTO toClassDTO(ClassEntity entity) {
        if (entity == null) return null;

        ClassDTO dto = new ClassDTO();

        dto.setId(entity.getId());
        dto.setClassName(entity.getClassName());
        dto.setSubject(entity.getSubject());
        dto.setType(entity.getType());
        dto.setStatus(entity.getStatus());
        dto.setRejectReason(entity.getRejectReason());
        
        //  MAP CREATED AT 
        dto.setCreatedAt(entity.getCreatedAt());

        dto.setTeacher(toTeacherDTO(entity.getTeacher()));

        dto.setSchedules(entity.getSchedules() != null
                ? entity.getSchedules().stream()
                        .map(DTOMapper::toScheduleDTO)
                        .collect(Collectors.toList())
                : List.of());

        dto.setStudents(entity.getStudents() != null
                ? entity.getStudents().stream()
                        .map(DTOMapper::toStudentDTO)
                        .collect(Collectors.toList())
                : List.of());

        dto.setStudentCount(entity.getStudentCount());

        return dto;
        }


    /**
     * Convert TeacherEntity → TeacherDTO
     * KHÔNG bao gồm trường classes (tránh circular ref)
     */
    public static TeacherDTO toTeacherDTO(TeacherEntity entity) {
        if (entity == null) return null;
        
        return new TeacherDTO(
                entity.getId(),
                entity.getFullName(),
                entity.getSubject(),
                entity.getStatus()
        );
    }

    /**
     * Convert ScheduleEntity → ScheduleDTO
     * Convert LocalTime → String (HH:mm:ss format)
     */
    public static ScheduleDTO toScheduleDTO(ScheduleEntity entity) {
        if (entity == null) return null;
        
        String startTimeStr = entity.getStartTime() != null ? entity.getStartTime().toString() : null;
        String endTimeStr = entity.getEndTime() != null ? entity.getEndTime().toString() : null;
        
        return new ScheduleDTO(
                entity.getId(),
                entity.getDayOfWeek(),
                startTimeStr,
                endTimeStr
        );
    }

    /**
     * Convert StudentEntity → StudentDTO
     * KHÔNG bao gồm trường classes (tránh circular ref)
     */
    public static StudentDTO toStudentDTO(StudentEntity entity) {
        if (entity == null) return null;
        
        return new StudentDTO(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getStatus()
        );
    }

    /**
     * Convert List of ClassEntity → List of ClassDTO
     */
    public static List<ClassDTO> toClassDTOList(List<ClassEntity> entities) {
        if (entities == null) return List.of();
        
        return entities.stream()
                .map(DTOMapper::toClassDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert List of TeacherEntity → List of TeacherDTO
     */
    public static List<TeacherDTO> toTeacherDTOList(List<TeacherEntity> entities) {
        if (entities == null) return List.of();
        
        return entities.stream()
                .map(DTOMapper::toTeacherDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert List of StudentEntity → List of StudentDTO
     */
    public static List<StudentDTO> toStudentDTOList(List<StudentEntity> entities) {
        if (entities == null) return List.of();
        
        return entities.stream()
                .map(DTOMapper::toStudentDTO)
                .collect(Collectors.toList());
    }
}
