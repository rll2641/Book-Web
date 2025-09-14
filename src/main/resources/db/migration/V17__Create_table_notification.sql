CREATE TABLE `notification_sub` (
    id int auto_increment primary key,
    user_id int not null,
    book_id int not null,
    threshold_quantity int not null default 5,
    is_active boolean not null default true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE
);

CREATE TABLE `notification_logs` (
    id int auto_increment primary key,
    sub_id int not null,
    user_id int not null,
    book_id int not null,
    threshold_quantity INT NOT NULL,
    current_stock INT NOT NULL,
    message text not null,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (sub_id) REFERENCES notification_sub(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE
);