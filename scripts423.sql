SELECT
    s.name  AS student_name,
    s.age   AS student_age,
    f.name  AS faculty_name
FROM student AS s
LEFT JOIN faculty AS f ON s.faculty_id = f.id;

SELECT
    s.name AS student_name
FROM student AS s
INNER JOIN avatar AS a ON a.student_id = s.id;