-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- 상품 테이블
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS auction (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   start_time TIMESTAMP NOT NULL,
   end_time TIMESTAMP NOT NULL,
   initial_price DECIMAL(19, 2) NOT NULL,
   minimum_bid_unit DECIMAL(19, 2) NOT NULL,
   status VARCHAR(20) NOT NULL,
   version INT NOT NULL DEFAULT 0,
   product_id BIGINT NOT NULL,
   user_id BIGINT NOT NULL,
   FOREIGN KEY (product_id) REFERENCES product(id),
   FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS bid (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   amount DECIMAL(19, 2) NOT NULL,
   created_at TIMESTAMP NOT NULL,
   auction_id BIGINT NOT NULL,
   user_id BIGINT NOT NULL,
   FOREIGN KEY (auction_id) REFERENCES auction(id),
   FOREIGN KEY (user_id) REFERENCES users(id)
);
