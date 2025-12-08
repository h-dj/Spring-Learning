package cn.hdj.jpa_tdd.service;

import cn.hdj.jpa_tdd.entity.Course;
import cn.hdj.jpa_tdd.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;


    @BeforeEach
    void setUp() {

    }

    @Test
    void findAllCourses_shouldReturnAllCourses() {
        // Given
        Course course1 = new Course();
        course1.setId(1L);
        course1.setName("Math");
        course1.setDescription("Mathematics course");

        Course course2 = new Course();
        course2.setId(2L);
        course2.setName("Physics");
        course2.setDescription("Physics course");

        List<Course> courses = Arrays.asList(course1, course2);

        when(courseRepository.findAll()).thenReturn(courses);

        // When
        List<Course> result = courseService.findAllCourses();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(course1, course2);
        
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    void findCourseById_shouldReturnCourse_whenCourseExists() {
        // Given
        Course course = new Course();
        course.setId(1L);
        course.setName("Math");
        course.setDescription("Mathematics course");

        // When
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));


        Optional<Course> result = courseService.findCourseById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Math");
        assertThat(result.get().getDescription()).isEqualTo("Mathematics course");
        
        verify(courseRepository, times(1)).findById(1L);
    }

    @Test
    void findCourseById_shouldReturnEmpty_whenCourseNotExists() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<Course> result = courseService.findCourseById(1L);

        // Then
        assertThat(result).isEmpty();
        
        verify(courseRepository, times(1)).findById(1L);
    }

    @Test
    void saveCourse_shouldSaveAndReturnCourse() {
        // Given
        Course course = new Course();
        course.setName("Math");
        course.setDescription("Mathematics course");
        
        Course savedCourse = new Course();
        savedCourse.setId(1L);
        savedCourse.setName("Math");
        savedCourse.setDescription("Mathematics course");
        
        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

        // When
        Course result = courseService.saveCourse(course);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Math");
        assertThat(result.getDescription()).isEqualTo("Mathematics course");
        
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void deleteCourseById_shouldCallDeleteMethod() {
        // When
        courseService.deleteCourseById(1L);

        // Then
        verify(courseRepository, times(1)).deleteById(1L);
    }
}