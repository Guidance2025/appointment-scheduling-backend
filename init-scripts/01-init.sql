DROP TABLE IF EXISTS tbl_notification CASCADE;
DROP TABLE IF EXISTS tbl_self_assessment CASCADE;
DROP TABLE IF EXISTS tbl_posts CASCADE;
DROP TABLE IF EXISTS tbl_anonymous_response CASCADE;
DROP TABLE IF EXISTS tbl_exit_interview CASCADE;
DROP TABLE IF EXISTS tbl_questions CASCADE;
DROP TABLE IF EXISTS tbl_moods CASCADE;
DROP TABLE IF EXISTS tbl_appointment CASCADE;
DROP TABLE IF EXISTS tbl_counseling_session CASCADE;
DROP TABLE IF EXISTS tbl_guidance_staff CASCADE;
DROP TABLE IF EXISTS tbl_student CASCADE;
DROP TABLE IF EXISTS tbl_category CASCADE;
DROP TABLE IF EXISTS tbl_login CASCADE;
DROP TABLE IF EXISTS tbl_user_device_token CASCADE;
DROP TABLE IF EXISTS tbl_person CASCADE;
DROP TABLE IF EXISTS tbl_section CASCADE;

-- Create tbl_section first (referenced by tbl_student)
CREATE TABLE tbl_section (
    section_id BIGSERIAL PRIMARY KEY,
    organization VARCHAR(64),
    cluster_name VARCHAR(128),
    section_name VARCHAR(64),
    cluster_head VARCHAR(64),
    course VARCHAR(128)
);

CREATE TABLE tbl_person (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50),
    middle_name VARCHAR(35),
    last_name VARCHAR(35),
    age SMALLINT,
    birthdate DATE,
    gender VARCHAR(10),
    email VARCHAR(64),
    address VARCHAR(255),
    contact_number VARCHAR(11)
);

CREATE TABLE tbl_user_device_token (
    token_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    device_type VARCHAR(64),
    fcm_token VARCHAR(64),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE tbl_login (
    login_id BIGSERIAL PRIMARY KEY,
    person_id BIGINT UNIQUE,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    join_date TIMESTAMP,
    last_login_date TIMESTAMP,
    role VARCHAR(64),
    authorities VARCHAR(255),
    is_active SMALLINT,
    is_locked SMALLINT
);

CREATE TABLE tbl_category (
    category_id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(64)
);

CREATE TABLE tbl_student (
    id BIGSERIAL PRIMARY KEY,
    student_number VARCHAR(10) UNIQUE,
    person_id BIGINT UNIQUE,
    section_id BIGINT
);

CREATE TABLE tbl_guidance_staff (
    employee_number BIGSERIAL PRIMARY KEY,
    person_id BIGINT,
    position_in_rc VARCHAR(64)
);

CREATE TABLE tbl_counseling_session (
    session_id BIGSERIAL PRIMARY KEY,
    student_id BIGINT,
    employee_number BIGINT,
    session_notes VARCHAR(255),
    session_status VARCHAR(32),
    session_type VARCHAR(128),
    session_date TIMESTAMP
);

CREATE TABLE tbl_appointment (
    appointment_id BIGSERIAL PRIMARY KEY,
    student_id BIGINT,
    employee_number BIGINT,
    scheduled_date TIMESTAMP,
    date_created TIMESTAMP,
    end_date TIMESTAMP,
    appointment_type VARCHAR(64),
    status VARCHAR(32),
    notes VARCHAR(128)
);

CREATE TABLE tbl_moods (
    mood_id BIGSERIAL PRIMARY KEY,
    student_id BIGINT,
    mood VARCHAR(64),
    entry_date TIMESTAMP,
    mood_notes VARCHAR(128)
);

CREATE TABLE tbl_questions (
    question_id BIGSERIAL PRIMARY KEY,
    category_id BIGINT,
    employee_number BIGINT,
    question_text VARCHAR(255),
    date_created DATE
);

CREATE TABLE tbl_exit_interview (
    interview_id BIGSERIAL PRIMARY KEY,
    student_id BIGINT,
    question_id BIGINT,
    response_text VARCHAR(255),
    submitted_date DATE
);

CREATE TABLE tbl_anonymous_response (
    response_id BIGSERIAL PRIMARY KEY,
    question_id BIGINT,
    person_id BIGINT,
    response_text VARCHAR(255),
    response_date DATE
);

CREATE TABLE tbl_posts (
    post_id BIGSERIAL PRIMARY KEY,
    employee_number BIGINT,
    section_id BIGINT,
    category_id BIGINT,
    question_id BIGINT,
    post_content VARCHAR(500),
    posted_date TIMESTAMP
);

CREATE TABLE tbl_self_assessment (
    assessment_response_id BIGSERIAL PRIMARY KEY,
    student_id BIGINT,
    question_id BIGINT,
    response_text VARCHAR(255),
    response_date TIMESTAMP
);

CREATE TABLE tbl_notification (
    notification_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    appointment_id BIGINT,
    message VARCHAR(255),
    action_type VARCHAR(64),
    is_read VARCHAR(64),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Foreign Keys
ALTER TABLE tbl_user_device_token
    ADD CONSTRAINT FK_TBL_PERSON_USER_ID
    FOREIGN KEY (user_id) REFERENCES tbl_login(login_id);

ALTER TABLE tbl_login
    ADD CONSTRAINT FK_TBL_LOGIN_PERSON_ID
    FOREIGN KEY (person_id) REFERENCES tbl_person(id);

ALTER TABLE tbl_guidance_staff
    ADD CONSTRAINT FK_TBL_EMPLOYEE_NUMBER_PERSON_ID
    FOREIGN KEY (person_id) REFERENCES tbl_person(id);

ALTER TABLE tbl_student
    ADD CONSTRAINT FK_TBL_STUDENT_SECTION_ID
    FOREIGN KEY (section_id) REFERENCES tbl_section(section_id);

ALTER TABLE tbl_student
    ADD CONSTRAINT FK_TBL_STUDENT_PERSON_ID
    FOREIGN KEY (person_id) REFERENCES tbl_person(id);

ALTER TABLE tbl_counseling_session
    ADD CONSTRAINT FK_TBL_COUNSELING_SESSION_STUDENT_ID
    FOREIGN KEY (student_id) REFERENCES tbl_student(id);

ALTER TABLE tbl_counseling_session
    ADD CONSTRAINT FK_TBL_COUNSELING_SESSION_EMPLOYEE_NUMBER
    FOREIGN KEY (employee_number) REFERENCES tbl_guidance_staff(employee_number);

ALTER TABLE tbl_appointment
    ADD CONSTRAINT FK_TBL_APPOINTMENT_STUDENT_NUMBER
    FOREIGN KEY (student_id) REFERENCES tbl_student(id);

ALTER TABLE tbl_appointment
    ADD CONSTRAINT FK_TBL_APPOINTMENT_EMPLOYEE_NUMBER
    FOREIGN KEY (employee_number) REFERENCES tbl_guidance_staff(employee_number);

ALTER TABLE tbl_moods
    ADD CONSTRAINT FK_TBL_MOODS_STUDENT_NUMBER
    FOREIGN KEY (student_id) REFERENCES tbl_student(id);

ALTER TABLE tbl_questions
    ADD CONSTRAINT FK_TBL_QUESTIONS_CATEGORY_ID
    FOREIGN KEY (category_id) REFERENCES tbl_category(category_id);

ALTER TABLE tbl_questions
    ADD CONSTRAINT FK_TBL_QUESTIONS_EMPLOYEE_NUMBER
    FOREIGN KEY (employee_number) REFERENCES tbl_guidance_staff(employee_number);

ALTER TABLE tbl_exit_interview
    ADD CONSTRAINT FK_TBL_EXIT_INTERVIEW_STUDENT_ID
    FOREIGN KEY (student_id) REFERENCES tbl_student(id);

ALTER TABLE tbl_exit_interview
    ADD CONSTRAINT FK_TBL_EXIT_INTERVIEW_QUESTION_ID
    FOREIGN KEY (question_id) REFERENCES tbl_questions(question_id);

ALTER TABLE tbl_anonymous_response
    ADD CONSTRAINT FK_TBL_ANONYMOUS_RESPONSE_QUESTION_ID
    FOREIGN KEY (question_id) REFERENCES tbl_questions(question_id);

ALTER TABLE tbl_posts
    ADD CONSTRAINT FK_TBL_POSTS_EMPLOYEE_NUMBER
    FOREIGN KEY (employee_number) REFERENCES tbl_guidance_staff(employee_number);

ALTER TABLE tbl_posts
    ADD CONSTRAINT FK_TBL_POSTS_SECTION_ID
    FOREIGN KEY (section_id) REFERENCES tbl_section(section_id);

ALTER TABLE tbl_posts
    ADD CONSTRAINT FK_TBL_POSTS_CATEGORY_ID
    FOREIGN KEY (category_id) REFERENCES tbl_category(category_id);

ALTER TABLE tbl_posts
    ADD CONSTRAINT FK_TBL_POSTS_QUESTION_ID
    FOREIGN KEY (question_id) REFERENCES tbl_questions(question_id);

ALTER TABLE tbl_self_assessment
    ADD CONSTRAINT FK_TBL_SELF_ASSESSMENT_STUDENT_ID
    FOREIGN KEY (student_id) REFERENCES tbl_student(id);

ALTER TABLE tbl_self_assessment
    ADD CONSTRAINT FK_TBL_SELF_ASSESSMENT_QUESTION_ID
    FOREIGN KEY (question_id) REFERENCES tbl_questions(question_id);

ALTER TABLE tbl_notification
    ADD CONSTRAINT FK_TBL_NOTIFICATION_USER_ID
    FOREIGN KEY (user_id) REFERENCES tbl_login(login_id);

ALTER TABLE tbl_notification
    ADD CONSTRAINT FK_TBL_NOTIFICATION_APPOINTMENT_ID
    FOREIGN KEY (appointment_id) REFERENCES tbl_appointment(appointment_id);

