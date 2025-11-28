drop user guidance cascade;

create user guidance identified by Changeme0;

alter user guidance quota unlimited on DATA;

alter user guidance quota unlimited on USERS;

grant create session to guidance with admin option;

grant connect to guidance;

alter session set current_schema = guidance;
drop table tbl_person cascade constraints;

drop table tbl_user_device_token cascade constraints;
drop table tbl_login cascade constraints;
drop table tbl_section cascade constraints;
drop table tbl_category cascade constraints;
drop table tbl_student cascade constraints;
drop table tbl_guidance_staff cascade constraints;
drop table tbl_counseling_session cascade constraints;
drop table tbl_appointment cascade constraints;
drop table tbl_moods cascade constraints;
drop table tbl_questions cascade constraints;
drop table tbl_exit_interview cascade constraints;
drop table tbl_anonymous_response cascade constraints;
drop table tbl_posts cascade constraints;
drop table tbl_self_assessment_response cascade constraints;
drop table tbl_notification cascade constraints;

create table tbl_person (
    id number(20,0) generated as identity
        constraint TBL_PERSON_ID_NOT_NULL not null,
    first_name varchar2(50 char),
    middle_name varchar2(35 char),
    last_name varchar2(35 char),
    age number(2,0),
    birthdate date,
    gender varchar(10 char),
    email varchar(64 char),
    address varchar2(255 char),
    contact_number varchar2(11),
    primary key (id));

create table tbl_user_device_token (
    token_id number(20,0) generated as identity,
    user_id number(20,0),
    device_type varchar2(64 char),
    fcm_token varchar2(64 char),
    created_at timestamp(6),
    updated_at timestamp(6),
    primary key (token_id));

create table tbl_login (
    login_id number(20,0) generated as identity
        constraint TBL_LOGIN_ID_NOT_NULL not null,
    person_id number(20,0) unique,
    username varchar2(64 char)
        constraint TBL_LOGIN_USERNAME_NOT_NULL not null,
    password varchar2(255 char)
        constraint TBL_LOGIN_PASSWORD_NOT_NULL not null,
    join_date timestamp(6),
    last_login_date timestamp(6),
    role varchar2(64 char),
    authorities varchar2(255 char),
    is_active number(1,0),
    is_locked number(1,0),
    primary key (login_id));

create table tbl_section (
    section_id number(20,0) generated as identity
        constraint TBL_SECTION_ID_NOT_NULL not null,
    organization varchar2(64 char),
    cluster_name varchar2(255 char),
    section_name varchar2(16 char),
    cluster_head varchar2(64 char),
    course varchar2(255 char),
    primary key (section_id));

create table tbl_category (
    category_id number(20,0) generated as identity
        constraint TBL_CATEGORY_ID_NOT_NULL not null,
    category_name varchar2(64 char),
    primary key (category_id));

create table tbl_student (
    id number(20,0) generated as identity,
    student_number varchar2(10 char) unique,
    person_id number(20,0) unique,
    section_id number(20,0),
    primary key (id));

create table tbl_guidance_staff (
    employee_number number(20,0) generated as identity,
    person_id number(20,0),
    position_in_rc varchar2(64 char),
    primary key (employee_number));

create table tbl_counseling_session (
    session_id number(20,0) generated as identity
        constraint TBL_SESSION_ID_NOT_NULL not null,
    student_id number(20,0),
    employee_number number(20,0),
    session_notes varchar2(255 char),
    session_status varchar2(32 char),
    session_type varchar2(128 char),
    session_date timestamp(6),
    primary key (session_id));

create table tbl_appointment (
    appointment_id number(20,0) generated as identity
        constraint TBL_APPOINTMENT_ID_NOT_NULL not null,
    student_id number(20,0),
    employee_number number(20,0),
    scheduled_date timestamp(6),
    date_created timestamp(6),
    end_date timestamp(6),
    appointment_type varchar2(64 char),
    status varchar2(32 char),
    notes varchar2(128 char),
    primary key (appointment_id));

create table tbl_moods (
    mood_id number(20,0) generated as identity
        constraint TBL_MOODS_ID_NOT_NULL not null,
    student_id number(20,0),
    mood varchar2(64 char),
    entry_date timestamp(6),
    mood_notes varchar2(128),
    primary key (mood_id));

create table tbl_questions (
    question_id number(20,0) generated as identity
        constraint TBL_QUESTION_ID_NOT_NULL not null,
    category_id number(20,0),
    employee_number number(20,0),
    question_text varchar2(255 char),
    date_created date,
    primary key (question_id));

create table tbl_exit_interview (
    interview_id number(20,0) generated as identity
        constraint TBL_INTERVIEW_ID_NOT_NULL not null,
    student_id number (20,0),
    question_id number (20,0),
    response_text varchar2(255 char),
    submitted_date date,
    primary key (interview_id));

create table tbl_anonymous_response (
    response_id number(20,0) generated as identity
        constraint TBL_RESPONSE_ID_NOT_NULL not null,
    question_id number(20,0),
    person_id number (20,0),
    response_text varchar2(255 char),
    response_date date,
    primary key (response_id));

create table tbl_posts (
    post_id number(20,0) generated as identity
        constraint TBL_POST_ID_NOT_NULL not null,
    employee_number number(20,0),
    section_id number(20,0),
    category_id number(20,0),
    question_id number(20,0),
    post_content varchar2(500 char),
    posted_date timestamp(6),
    primary key (post_id));

create table tbl_self_assessment (
    assessment_response_id number(20,0) generated as identity
        constraint TBL_ASSESSMENT_RESPONSE_ID_NOT_NULL not null,
    student_id number (20,0),
    question_id number (20,0),
    response_text varchar2(255 char),
    response_date timestamp(6),
    primary key (assessment_response_id));

create table tbl_notification (
    notification_id number(20,0) generated as identity,
    user_id number(20,0),
    appointment_id number(20,0),
    message varchar2(255),
    action_type varchar2(64 char),
    is_read varchar2(64 char),
    created_at timestamp(6),
    updated_at timestamp(6),
    primary key (notification_id));

alter table tbl_user_device_token
    add constraint FK_TBL_PERSON_USER_ID
    foreign key (user_id) references tbl_login;

alter table tbl_login
    add constraint FK_TBL_LOGIN_PERSON_ID
    foreign key (person_id) references tbl_person;

alter table tbl_guidance_staff
    add constraint FK_TBL_EMPLOYEE_NUMBER_PERSON_ID
    foreign key (person_id) references tbl_person;

alter table tbl_student
    add constraint FK_TBL_STUDENT_SECTION_ID
    foreign key (section_id) references tbl_section;

alter table tbl_student
    add constraint FK_TBL_STUDENT_PERSON_ID
    foreign key (person_id) references tbl_person;

alter table tbl_counseling_session
     add constraint FK_TBL_COUNSELING_SESSION_STUDENT_ID
     foreign key (student_id) references tbl_student;

alter table tbl_counseling_session
    add constraint FK_TBL_COUNSELING_SESSION_EMPLOYEE_NUMBER
    foreign key (employee_number) references tbl_guidance_staff;

alter table tbl_appointment
     add constraint FK_TBL_APPOINTMENT_STUDENT_NUMBER
     foreign key (student_id) references tbl_student;

alter table tbl_appointment
    add constraint FK_TBL_APPOINTMENT_EMPLOYEE_NUMBER
    foreign key (employee_number) references tbl_guidance_staff;

alter table tbl_moods
     add constraint FK_TBL_MOODS_STUDENT_NUMBER
     foreign key (student_id) references tbl_student;

alter table tbl_questions
        add constraint FK_TBL_QUESTIONS_CATEGORY_ID
    foreign key (category_id) references tbl_category;

alter table tbl_questions
    add constraint FK_TBL_QUESTIONS_EMPLOYEE_NUMBER
    foreign key (employee_number) references tbl_guidance_staff;

alter table tbl_exit_interview
    add constraint FK_TBL_EXIT_INTERVIEW_STUDENT_ID
    foreign key (student_id) references tbl_student;

alter table tbl_exit_interview
    add constraint FK_TBL_EXIT_INTERVIEW_QUESTION_ID
    foreign key (question_id) references tbl_questions;

alter table tbl_anonymous_response
    add constraint FK_TBL_ANONYMOUS_RESPONSE_QUESTION_ID
    foreign key (question_id) references tbl_questions;

alter table tbl_posts
    add constraint FK_TBL_POSTS_EMPLOYEE_NUMBER
    foreign key (employee_number) references tbl_guidance_staff;

alter table tbl_posts
    add constraint FK_TBL_POSTS_SECTION_ID
    foreign key (section_id) references tbl_section;

alter table tbl_posts
        add constraint FK_TBL_POSTS_CATEGORY_ID
    foreign key (category_id) references tbl_category;

alter table tbl_posts
    add constraint FK_TBL_POSTS_QUESTION_ID
    foreign key (question_id) references tbl_questions;

alter table tbl_self_assessment
    add constraint FK_TBL_SELF_ASSESSMENT_STUDENT_ID
    foreign key (student_id) references tbl_student;

alter table tbl_self_assessment
    add constraint FK_TBL_SELF_ASSESSMENT_QUESTION_ID
    foreign key (question_id) references tbl_questions;

alter table tbl_notification
    add constraint FK_TBL_NOTIFICATION_USER_ID
    foreign key (user_id) references tbl_login;

alter table tbl_notification
    add constraint FK_TBL_NOTIFICATION_APPOINTMENT_ID
    foreign key (appointment_id) references tbl_appointment;

--TEST DATA
-- INSERT PERSON TABLE
insert into tbl_person (first_name, middle_name, last_name, age, birthdate, gender, email, address, contact_number)
values ('Anna', 'Marie', 'Santos', 19, to_date('2006-03-15','YYYY-MM-DD'), 'Female', 'anna.santos@student.edu', '123 Mabini St, Manila', '09171234567');
insert into tbl_person (first_name, middle_name, last_name, age, birthdate, gender, email, address, contact_number)
values ('John', 'Rey', 'Cruz', 20, to_date('2005-07-22','YYYY-MM-DD'), 'Male', 'john.cruz@student.edu', '456 Rizal Ave, Quezon City', '09181234567');
insert into tbl_person (first_name, middle_name, last_name, age, birthdate, gender, email, address, contact_number)
values ('Liza', 'Mae', 'Torres', 18, to_date('2007-01-10','YYYY-MM-DD'), 'Female', 'liza.torres@student.edu', '789 Bonifacio St, Makati', '09191234567');
insert into tbl_person (first_name, middle_name, last_name, age, birthdate, gender, email, address, contact_number)
values ('Mark', 'Anthony', 'Dela Cruz', 21, to_date('2004-09-05','YYYY-MM-DD'), 'Male', 'mark.delacruz@student.edu', '321 Aguinaldo Blvd, Pasig', '09201234567');
insert into tbl_person (first_name, middle_name, last_name, age, birthdate, gender, email, address, contact_number)
values ('Ella', 'Grace', 'Navarro', 19, to_date('2006-06-30','YYYY-MM-DD'), 'Female', 'ella.navarro@student.edu', '654 Katipunan Rd, Marikina', '09211234567');
insert into tbl_person (first_name, middle_name, last_name, age, birthdate, gender, email, address, contact_number)
values ('Carlos', 'Miguel', 'Reyes', 35, to_date('1990-11-12','YYYY-MM-DD'), 'Male', 'carlos.reyes@guidance.edu', '12 Kalayaan St, Taguig', '09301234567');
insert into tbl_person (first_name, middle_name, last_name, age, birthdate, gender, email, address, contact_number)
values ('Diana', 'Rose', 'Lopez', 32, to_date('1993-04-18','YYYY-MM-DD'), 'Female', 'diana.lopez@guidance.edu', '34 Commonwealth Ave, QC', '09311234567');
insert into tbl_person (first_name, middle_name, last_name, age, birthdate, gender, email, address, contact_number)
values ('Samuel', 'Lee', 'Tan', 40, to_date('1985-08-25','YYYY-MM-DD'), 'Male', 'samuel.tan@guidance.edu', '56 Ortigas Ext, Pasig', '09321234567');
insert into tbl_person (first_name, middle_name, last_name, age, birthdate, gender, email, address, contact_number)
values ('Therese', 'Joy', 'Gomez', 29, to_date('1996-02-03','YYYY-MM-DD'), 'Female', 'therese.gomez@guidance.edu', '78 Shaw Blvd, Mandaluyong', '09331234567');
insert into tbl_person (first_name, middle_name, last_name, age, birthdate, gender, email, address, contact_number)
values ('Miguel', 'Andres', 'Villanueva', 38, to_date('1987-12-20','YYYY-MM-DD'), 'Male', 'miguel.villanueva@guidance.edu', '90 Taft Ave, Manila', '09341234567');

-- INSERT LOGIN TABLE
insert into tbl_login (username, password, person_id, join_date, last_login_date, role, authorities, is_active, is_locked)
values ('anna.santos', '$2a$10$JKotLEwO8PMsnr3.ufngYusceu4T7UHBSkFgeyrv/q0.WPiGm9DxS', 1, to_timestamp('2024-06-01 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), to_timestamp('2025-08-01 09:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'ROLE_USER', 'user:read,user:create', 1, 0);
insert into tbl_login (username, password, person_id, join_date, last_login_date, role, authorities, is_active, is_locked)
values('john.cruz', '$2a$10$JKotLEwO8PMsnr3.ufngYusceu4T7UHBSkFgeyrv/q0.WPiGm9DxS', 2, to_timestamp('2024-06-02 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), to_timestamp('2025-08-02 09:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'ROLE_USER', 'user:read,user:create', 1, 0);
insert into tbl_login (username, password, person_id, join_date, last_login_date, role, authorities, is_active, is_locked)
values('liza.torres', '$2a$10$JKotLEwO8PMsnr3.ufngYusceu4T7UHBSkFgeyrv/q0.WPiGm9DxS', 3, to_timestamp('2024-06-03 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), to_timestamp('2025-08-03 09:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'ROLE_USER', 'user:read,user:create', 1, 0);
insert into tbl_login (username, password, person_id, join_date, last_login_date, role, authorities, is_active, is_locked)
values('mark.delacruz', '$2a$10$JKotLEwO8PMsnr3.ufngYusceu4T7UHBSkFgeyrv/q0.WPiGm9DxS', 4, to_timestamp('2024-06-04 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), to_timestamp('2025-08-04 09:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'ROLE_USER', 'user:read,user:create', 1, 0);
insert into tbl_login (username, password, person_id, join_date, last_login_date, role, authorities, is_active, is_locked)
values('ella.navarro', '$2a$10$JKotLEwO8PMsnr3.ufngYusceu4T7UHBSkFgeyrv/q0.WPiGm9DxS', 5, to_timestamp('2024-06-05 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), to_timestamp('2025-08-05 09:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'ROLE_USER', 'user:read,user:create', 1, 0);
insert into tbl_login (username, password, person_id, join_date, last_login_date, role, authorities, is_active, is_locked)
values ('carlos.reyes', '$2a$10$JKotLEwO8PMsnr3.ufngYusceu4T7UHBSkFgeyrv/q0.WPiGm9DxS', 6, to_timestamp('2024-01-01 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), to_timestamp('2025-08-01 09:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'ROLE_STAFF', 'user:read,user:create,user:update', 1, 0);
insert into tbl_login (username, password, person_id, join_date, last_login_date, role, authorities, is_active, is_locked)
values('diana.lopez', '$2a$10$JKotLEwO8PMsnr3.ufngYusceu4T7UHBSkFgeyrv/q0.WPiGm9DxS', 7, to_timestamp('2024-01-02 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), to_timestamp('2025-08-02 09:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'ROLE_STAFF', 'user:read,user:create,user:update', 1, 0);
insert into tbl_login (username, password, person_id, join_date, last_login_date, role, authorities, is_active, is_locked)
values('samuel.tan', '$2a$10$JKotLEwO8PMsnr3.ufngYusceu4T7UHBSkFgeyrv/q0.WPiGm9DxS', 8, to_timestamp('2024-01-03 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), to_timestamp('2025-08-03 09:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'ROLE_STAFF', 'user:read,user:create,user:update', 1, 0);
insert into tbl_login (username, password, person_id, join_date, last_login_date, role, authorities, is_active, is_locked)
values('therese.gomez', '$2a$10$JKotLEwO8PMsnr3.ufngYusceu4T7UHBSkFgeyrv/q0.WPiGm9DxS', 9, to_timestamp('2024-01-04 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), to_timestamp('2025-08-04 09:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'ROLE_STAFF', 'user:read,user:create,user:update', 1, 0);
insert into tbl_login (username, password, person_id, join_date, last_login_date, role, authorities, is_active, is_locked)
values('miguel.villanueva', '$2a$10$JKotLEwO8PMsnr3.ufngYusceu4T7UHBSkFgeyrv/q0.WPiGm9DxS', 10, to_timestamp('2024-01-05 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), to_timestamp('2025-08-05 09:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'ROLE_STAFF', 'user:read,user:create,user:update', 1, 0);

--INSERT SECTION TABLE
insert into tbl_section (organization, cluster_name, section_name, cluster_head, course)
values ('ROCS', 'Cluster of Engineering, Technology, and Education', 'BSIT-701', 'Ms Darleen Gener', 'Bachelor of Science in Information Technology');
insert into tbl_section (organization, cluster_name, section_name, cluster_head, course)
values('ELITES', 'Cluster of Engineering, Technology, and Education', 'BSIT-701', 'Ms Darleen Gener', 'Bachelor of Science in Electronics Engineering');
insert into tbl_section (organization, cluster_name, section_name, cluster_head, course)
values('MERX', 'Cluster of Business, Accountancy, and Management', 'BSIT-701', 'Mr. John Cruz', 'Bachelor of Science in Business Administration major in Marketing Management');

--INSERT CATEGORY TABLE
insert into tbl_category (category_name)
values ('Exit Interview');
insert into tbl_category (category_name)
values('Posts');
insert into tbl_category (category_name)
values('Qoute');
insert into tbl_category (category_name)
values('Self-Assessment');

--INSERT STUDENT TABLE
insert into tbl_student (student_number, person_id, section_id)
values ('CT22-0079', 1, 1);
insert into tbl_student (student_number, person_id, section_id)
values('CT21-0058', 2, 1);
insert into tbl_student (student_number, person_id, section_id)
values('CT21-0025', 3, 2);
insert into tbl_student (student_number, person_id, section_id)
values('CT22-0016', 4, 2);
insert into tbl_student (student_number, person_id, section_id)
values('CT23-0012', 5, 3);

--INSERT GUIDANCE STAFF TABLE
insert into tbl_guidance_staff (person_id, position_in_rc)
values (6, 'Guidance Counselor');
insert into tbl_guidance_staff (person_id, position_in_rc)
values (7, 'Guidance Facilitator');
insert into tbl_guidance_staff (person_id, position_in_rc)
values (8, 'Guidance Facilitator');
insert into tbl_guidance_staff (person_id, position_in_rc)
values (9, 'Guidance Facilitator');
insert into tbl_guidance_staff (person_id, position_in_rc)
values (10, 'Guidance Facilitator');

--INSERT COUNSELING SESSION TABLE
insert into tbl_counseling_session (student_id, employee_number, session_notes, session_status, session_type, session_date)
values (2, 1, 'Discussed anxiety management.', 'Completed', 'Individual Counseling', to_timestamp('2025-07-01 10:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'));
insert into tbl_counseling_session (student_id, employee_number, session_notes, session_status, session_type, session_date)
values (5, 2, 'Explored career options.', 'Completed', 'Exit Interview', to_timestamp('2025-07-05 14:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'));

--INSERT APPOINTMENT TABLE
insert into tbl_appointment (student_id, employee_number, scheduled_date, date_created, appointment_type, status, notes)
values (2, 3, to_timestamp('2025-08-15 09:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), to_timestamp('2025-08-01 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'Individual Counseling', 'Scheduled', 'Check-in on progress');
insert into tbl_appointment (student_id, employee_number, scheduled_date, date_created, appointment_type, status, notes)
values(5, 4, to_timestamp('2025-08-20 13:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), to_timestamp('2025-08-02 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'Exit Interview', 'Scheduled', 'First session');

--INSERT MOODS TABLE
insert into tbl_moods (student_id, mood, entry_date, mood_notes)
values (2, 'Nervous', to_timestamp('2025-08-01 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'Exit Interview coming');
insert into tbl_moods (student_id, mood, entry_date, mood_notes)
values (5, 'Anxiety', to_timestamp('2025-08-02 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'), 'mental health guidance');

--INSERT QUESTIONS TABLE
insert into tbl_questions (category_id, employee_number, question_text, date_created)
values (1, 1, 'Are you confident in landing a job?', to_date('2025-07-01','YYYY-MM-DD'));
insert into tbl_questions (category_id, employee_number, question_text, date_created)
values (2, 2, 'Kamusta ka Kaibigan?', to_date('2025-07-02','YYYY-MM-DD'));
insert into tbl_questions (category_id, employee_number, question_text, date_created)
values (1, 3, 'What career paths interest you?', to_date('2025-07-03','YYYY-MM-DD'));
insert into tbl_questions (category_id, employee_number, question_text, date_created)
values (4, 3, 'Are you confident in your potential?', to_date('2025-07-03','YYYY-MM-DD'));

--INSERT EXIT INTERVIEW
insert into tbl_exit_interview (question_id, student_id, response_text, submitted_date)
values (1, 2, 'yes', to_date('2025-08-01','YYYY-MM-DD'));
insert into tbl_exit_interview (question_id, student_id, response_text, submitted_date)
values(1, 5, 'no', to_date('2025-08-02','YYYY-MM-DD'));
insert into tbl_exit_interview (question_id, student_id, response_text, submitted_date)
values (3, 2, 'inline with the BSIT', to_date('2025-08-01','YYYY-MM-DD'));
insert into tbl_exit_interview (question_id, student_id, response_text, submitted_date)
values(3, 5, 'inline with the BA course', to_date('2025-08-02','YYYY-MM-DD'));

--INSERT ANONYMOUS RESPONSE
insert into tbl_anonymous_response (question_id, response_text, response_date)
values (2, 'as of now, im not ok,  i just want to rest for a long time.', to_date('2025-08-03','YYYY-MM-DD'));
insert into tbl_anonymous_response (question_id, response_text, response_date)
values (2, 'i feel ok and confident', to_date('2025-08-03','YYYY-MM-DD'));
insert into tbl_anonymous_response (question_id, response_text, response_date)
values (2, 'super tired.', to_date('2025-08-03','YYYY-MM-DD'));

--INSERT POSTS TABLE
insert into tbl_posts (employee_number, section_id, category_id, question_id, post_content, posted_date)
values (1, 1, 2, 2, 'Tips for managing exam stress—share your strategies!', to_timestamp('2025-08-01 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'));
insert into tbl_posts (employee_number, section_id, category_id, question_id, post_content, posted_date)
values(2, 2, 2, 2, 'Let’s talk about study habits that work.', to_timestamp('2025-08-02 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'));

--INSERT SELF-ASSESSMENT TABLE
insert into tbl_self_assessment (student_id, question_id, response_text, response_date)
values (3, 4, 'I feel overwhelmed but coping.', to_timestamp('2025-08-01 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'));
insert into tbl_self_assessment (student_id, question_id, response_text, response_date)
values (4, 4, 'Yes.', to_timestamp('2025-08-02 08:00:00.00', 'yyyy-mm-dd hh24:mi:ss:ff'));

commit;