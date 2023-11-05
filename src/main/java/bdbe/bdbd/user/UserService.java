package bdbe.bdbd.user;



import bdbe.bdbd._core.errors.exception.BadRequestError;
import bdbe.bdbd._core.errors.exception.InternalServerError;
import bdbe.bdbd._core.errors.security.JWTProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserJPARepository userJPARepository;

    @Transactional
    public void join(UserRequest.JoinDTO requestDTO) {
        sameCheckEmail(requestDTO.getEmail());

        String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());

        try {
            userJPARepository.save(requestDTO.toEntity(encodedPassword));
        } catch (Exception e) {
            throw new InternalServerError("unknown server error");
        }
    }

//    public String login(UserRequest.LoginDTO requestDTO) {
//        User userPS = userJPARepository.findByEmail(requestDTO.getEmail()).orElseThrow(
//                () -> new Exception400("이메일을 찾을 수 없습니다 : "+requestDTO.getEmail())
//        );
//
//        if(!passwordEncoder.matches(requestDTO.getPassword(), userPS.getPassword())){
//            throw new Exception400("패스워드가 잘못입력되었습니다.");
//        }
//        return JWTProvider.create(userPS);
//    }

    public UserResponse.LoginResponse login(UserRequest.LoginDTO requestDTO) {
        User userPS = userJPARepository.findByEmail(requestDTO.getEmail()).orElseThrow(
                () -> new BadRequestError("email not found : " + requestDTO.getEmail())
        );

        if (!passwordEncoder.matches(requestDTO.getPassword(), userPS.getPassword())) {
            throw new BadRequestError("wrong password");
        }

        String jwt = JWTProvider.create(userPS);
        String redirectUrl = "/user/home";

        return new UserResponse.LoginResponse(jwt, redirectUrl);
    }


    public void sameCheckEmail(String email) {
        Optional<User> userOP = userJPARepository.findByEmail(email);
        if (userOP.isPresent()) {
            throw new BadRequestError("duplicate email exist : " + email);
        }
    }

    /*
        토큰으로 전달받은 Member 객체의 ID를 이용하여 데이터베이스에서 해당 멤버의 전체 정보를 조회하는 메서드
     */
    public OwnerResponse.UserInfoDTO findUserInfo(Member member) {
        Member findMember = memberJPARepository.findById(member.getId())
                .orElseThrow(() -> new BadRequestError("member not found"));

        return new OwnerResponse.UserInfoDTO(findMember);
    }
}