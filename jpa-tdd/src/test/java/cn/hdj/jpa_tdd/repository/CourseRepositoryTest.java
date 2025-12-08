package cn.hdj.jpa_tdd.repository;

import cn.hdj.jpa_tdd.entity.Course;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void should_save_course() {
        // Given
        Course course = new Course();
        course.setName("Math");
        course.setDescription("Mathematics course");

        // When
        Course savedCourse = courseRepository.save(course);

        // Then
        assertThat(savedCourse.getId()).isNotNull();
        assertThat(savedCourse.getName()).isEqualTo("Math");
        assertThat(savedCourse.getDescription()).isEqualTo("Mathematics course");
    }

    @Test
    void should_find_course_by_id() {
        // Given
        Course course = new Course();
        course.setName("Math");
        course.setDescription("Mathematics course");
        Course savedCourse = entityManager.persistAndFlush(course);

        // When
        Optional<Course> foundCourse = courseRepository.findById(savedCourse.getId());

        // Then
        assertThat(foundCourse).isPresent();
        assertThat(foundCourse.get().getName()).isEqualTo("Math");
        assertThat(foundCourse.get().getDescription()).isEqualTo("Mathematics course");
    }

    @Test
    void should_find_all_courses() {
        // Given
        Course course1 = new Course();
        course1.setName("Math");
        course1.setDescription("Mathematics course");
        entityManager.persistAndFlush(course1);

        Course course2 = new Course();
        course2.setName("Physics");
        course2.setDescription("Physics course");
        entityManager.persistAndFlush(course2);

        // When
        List<Course> courses = courseRepository.findAll();

        // Then
        assertThat(courses).hasSize(2);
    }

    @Test
    void should_delete_course_by_id() {
        // Given
        Course course = new Course();
        course.setName("Math");
        course.setDescription("Mathematics course");
        Course savedCourse = entityManager.persistAndFlush(course);

        // When
        courseRepository.deleteById(savedCourse.getId());
        Optional<Course> foundCourse = courseRepository.findById(savedCourse.getId());

        // Then
        assertThat(foundCourse).isEmpty();
    }
}