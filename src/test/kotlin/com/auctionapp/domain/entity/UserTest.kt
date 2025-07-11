package com.auctionapp.domain.entity

import com.auctionapp.domain.exception.InvalidEmailException
import com.auctionapp.domain.exception.InvalidUserNameException
import com.auctionapp.domain.vo.Email
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
class UserTest {
    @Test
    @DisplayName("적절한 이메일 형식이 아니라면 실패한다")
    fun userInvalidEmailTest() {
        // when & then
        assertThrows<InvalidEmailException> {
            Email("test")
        }
    }

    @Test
    @DisplayName("이름이 비어있으면 실패한다")
    fun userBlankNameTest() {
        // given
        val blankName = ""

        // when & then
        assertThrows<InvalidUserNameException> {
            User.fixture(name = blankName)
        }
    }

    @Test
    @DisplayName("적절한 이메일, 비밀 번호, 이름이 있으면 성공한다")
    fun userTest() {
        // given
        val validEmail = Email("test@google.com")
        val validPassword = "Test12345!@"
        val name = "tester"

        // when
        val user =
            User(email = validEmail, encodedPassword = validPassword, name = name, role = Role.CUSTOMER)

        // then
        assertThat(user.email).isEqualTo(validEmail)
        assertThat(user.password).isEqualTo(validPassword)
        assertThat(user.name).isEqualTo(name)
    }

    @Test
    @DisplayName("객체 생성 후 유효하지 않은 이메일로 변경하면 실패한다")
    fun changeInvalidEmailAfterCreationTest() {
        // given
        val user = User.fixture()

        // when & then
        assertThrows<InvalidEmailException> {
            user.email = Email("invalid-email")
        }
    }

    @Test
    @DisplayName("객체 생성 후 빈 이름으로 변경하면 실패한다")
    fun changeBlankNameAfterCreationTest() {
        // given
        val user = User.fixture()
        val blankName = ""

        // when & then
        assertThrows<InvalidUserNameException> {
            user.name = blankName
        }
    }

    @Test
    @DisplayName("객체 생성 후 유효한 값으로 프로퍼티를 변경하면 성공한다")
    fun changeValidPropertiesAfterCreationTest() {
        // given
        val user = User.fixture()
        val newEmail = Email("new@example.com")
        val newPassword = "NewPass123!"
        val newName = "New Name"

        // when
        user.email = newEmail
        user.password = newPassword
        user.name = newName

        // then
        assertThat(user.email).isEqualTo(newEmail)
        assertThat(user.password).isEqualTo(newPassword)
        assertThat(user.name).isEqualTo(newName)
    }
}
