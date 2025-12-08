package cn.hdj.jpa_tdd.service;

import cn.hdj.jpa_tdd.entity.Course;
import cn.hdj.jpa_tdd.entity.Student;
import cn.hdj.jpa_tdd.repository.CourseRepository;
import cn.hdj.jpa_tdd.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private StudentServiceImpl studentService; // 要测试的实现类

    @Test
    void addStudent_shouldCallSave_andReturnSavedEntityWithId() {
        // arrange
        Student s = new Student();
        s.setName("Lucy");
        // 模拟 repository 将 id 回填（通常 DB 会回填）
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> {
            Student arg = inv.getArgument(0);
            arg.setId(100L);
            return arg;
        });

        // act
        Student result = studentService.addStudent(s);

        // assert: 返回实体包含 id（表明已保存并返回）
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Lucy", result.getName());

        // verify: 确保 service 调用了 repository.save 并传入原对象
        verify(studentRepository, times(1)).save(s);
    }

    @Test
    public void create_new_student() {
        // Given
        Student student = new Student();
        student.setName("Tom");
        student.setAge(20);
        student.setClassName("Class A");

        when(studentRepository.save(any(Student.class))).thenReturn(student);

        // When
        Student result = studentService.addStudent(student);

        // Then
        assertNotNull(result);
        assertEquals("Tom", result.getName());
        assertEquals(Integer.valueOf(20), result.getAge());
        assertEquals("Class A", result.getClassName());

        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void should_throw_when_duplicate() {
        // Given
        Long studentId = 1L;
        Long courseId = 1L;

        Student student = new Student();
        student.setId(studentId);
        student.setName("Tom");
        student.setCourses(new ArrayList<>());

        Course course = new Course();
        course.setId(courseId);
        course.setName("Math");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // When & Then
        Student result = studentService.assignCourse(studentId, courseId);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCourses().size());
        assertEquals(courseId, result.getCourses().get(0).getId());

        // 验证方法调用
        verify(studentRepository, times(1)).findById(studentId);
        verify(courseRepository, times(1)).findById(courseId);
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void should_throw_when_student_not_found() {
        // Given
        Long studentId = 1L;
        Long courseId = 1L;

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentService.assignCourse(studentId, courseId);
        });

        assertEquals("student not found", exception.getMessage());

        // 验证方法调用
        verify(studentRepository, times(1)).findById(studentId);
        verify(courseRepository, never()).findById(any());
        verify(studentRepository, never()).save(any());
    }

    @Test
    void should_throw_when_course_not_found() {
        // Given
        Long studentId = 1L;
        Long courseId = 1L;

        Student student = new Student();
        student.setId(studentId);
        student.setName("Tom");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentService.assignCourse(studentId, courseId);
        });

        assertEquals("course not found", exception.getMessage());

        // 验证方法调用
        verify(studentRepository, times(1)).findById(studentId);
        verify(courseRepository, times(1)).findById(courseId);
        verify(studentRepository, never()).save(any());
    }


    @Captor
    ArgumentCaptor<Student> captor;

    @Test
    void should_capture_argument() {
        Student student = new Student();
        student.setId(1L);
        student.setName("Tom");
        student.setAge(18);

        when(studentRepository.save(any())).thenReturn(student);

        studentService.addStudent(student);

        verify(studentRepository).save(captor.capture());
        Student value = captor.getValue();

        assertEquals("Tom", value.getName());
        assertEquals(18, value.getAge());
    }
}
