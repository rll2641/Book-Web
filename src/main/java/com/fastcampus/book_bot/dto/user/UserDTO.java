package com.fastcampus.book_bot.dto.user;

import com.fastcampus.book_bot.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Integer userId;
    private UserGradeDTO userGradeDTO;
    private String userEmail;
    private String userName;
    private String userNickname;
    private String userPhone;
    private Integer point;
    private Integer postcode;
    private String defaultAddress;
    private String detailAddress;
    private String city;
    private String province;

    public UserDTO(User user) {
        this.userId = user.getUserId();
        this.userEmail = user.getUserEmail();
        this.userName = user.getUserName();
        this.userNickname = user.getUserNickname();
        this.userPhone = user.getUserPhone();
        this.point = user.getPoint();
        this.postcode = user.getPostcode();
        this.defaultAddress = user.getDefaultAddress();
        this.detailAddress = user.getDetailAddress();
        this.city = user.getCity();
        this.province = user.getProvince();

        // UserGrade를 UserGradeDTO로 변환
        if (user.getUserGrade() != null) {
            this.userGradeDTO = new UserGradeDTO(user.getUserGrade());
        }
    }
}