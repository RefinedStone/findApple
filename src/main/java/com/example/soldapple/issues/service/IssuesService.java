package com.example.soldapple.issues.service;

import com.example.soldapple.aws_s3.S3UploadUtil;
import com.example.soldapple.create_price.dto.GetIPhonePriceResDto;
import com.example.soldapple.create_price.dto.GetMacbookPriceResDto;
import com.example.soldapple.issues.dto.RequestDto.IssuesRequestDto;
import com.example.soldapple.issues.dto.ResponseDto.IssuesResponseDto;
import com.example.soldapple.issues.entity.Issues;
import com.example.soldapple.issues.entity.IssuesImage;
import com.example.soldapple.issues.repository.IssuesImageRepository;
import com.example.soldapple.issues.repository.IssuesRepository;
import com.example.soldapple.like.repository.IssuesLikeRepository;
import com.example.soldapple.member.entity.Member;
import com.example.soldapple.post.entity.Opt;
import com.example.soldapple.post.repository.OptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class IssuesService {
    private final IssuesRepository issuesRepository;
    private final S3UploadUtil s3UploadUtil;
    private final IssuesImageRepository issuesimageRepository;
    private final IssuesLikeRepository issuesLikeRepository;
    private final OptionRepository optionRepository;

    //이의제기글 작성
    public IssuesResponseDto createIssue(List<MultipartFile> multipartFiles,
                                         IssuesRequestDto issuesRequestDto,
                                         GetIPhonePriceResDto iphoneOption,
                                         GetMacbookPriceResDto macbookOption,
                                         Member member) throws IOException{
        Issues issues = new Issues(issuesRequestDto, member);
        issuesRepository.save(issues);

        //맥북일 때
        if (iphoneOption==null){
            Opt options = new Opt(macbookOption, issues);
            optionRepository.save(options);
            return imgSave(multipartFiles, issues, member,options);
        } else{
            //아이폰일 때
            Opt options = new Opt(iphoneOption, issues);
            optionRepository.save(options);
            return imgSave(multipartFiles, issues, member,options);
        }

    }

    //이의제기글 수정
    public IssuesResponseDto updateIssue(Long issuesId,IssuesRequestDto issuesRequestDto, Member member) {
        Issues issues = issuesRepository.findByIssuesIdAndMember(issuesId, member).orElseThrow(
                ()->new IllegalArgumentException("해당 이의제기 글이 존재하지 않거나 수정 권한이 없습니다.")
        );
        issues.update(issuesRequestDto);
        issuesRepository.save(issues);

        return putImgsAndLikeToDto(issues, member);
    }

    //이의제기글 삭제
    public String deleteIssue(Long issuesId, Member member) {
        Issues issues = issuesRepository.findByIssuesIdAndMember(issuesId, member).orElseThrow(
                ()->new IllegalArgumentException("해당 이의제기 글이 존재하지 않거나 삭제 권한이 없습니다.")
        );
        deleteImgAndIssues(issues);
        return "이의제기글 삭제 완료";
    }

    //이의제기글 전체 조회
    public List<IssuesResponseDto> allIssues(Member member) {
        List<Issues> issues = issuesRepository.findAllByOrderByCreatedAtDesc();
        List<IssuesResponseDto> issuesResponseDtos = new ArrayList<IssuesResponseDto>();
        for (Issues issue : issues) {
            issuesResponseDtos.add(putImgsAndLikeToDto(issue, member));
        }
        return issuesResponseDtos;
    }

    //이의제기글 하나 조회
    public IssuesResponseDto oneIssue(Long issuesId, Member member) {
        Issues issue = issuesRepository.findByIssuesId(issuesId).orElseThrow(
                () -> new IllegalArgumentException("해당 이의제기 글이 존재하지 않습니다.")
        );
        return putImgsAndLikeToDto(issue, member);
    }

    //게시글 카테고리 조회
    public List<IssuesResponseDto> categoryPost(String category, Member member) {
        List<IssuesResponseDto> issuesResponseDtos = new ArrayList<IssuesResponseDto>();
        List<Issues> issues = issuesRepository.findAllByCategoryOrderByCreatedAtDesc(category).orElseThrow(
                ()->new RuntimeException("해당 카테고리가 없습니다.")
        );
        for (Issues issue : issues) {
            issuesResponseDtos.add(putImgsAndLikeToDto(issue, member));
        }
        return issuesResponseDtos;
    }

////반복되는 로직 메소드
    //이미지 저장
    public IssuesResponseDto imgSave(List<MultipartFile> multipartFiles, Issues issues, Member member, Opt opt) throws IOException {
        List<IssuesImage> imageList = new ArrayList<>();

        if(!(multipartFiles.size()==0)){
            System.out.println(multipartFiles.get(0).getOriginalFilename());
            for(MultipartFile imgFile : multipartFiles){
                Map<String, String> img = s3UploadUtil.upload(imgFile, "test");
                IssuesImage issuesImage = new IssuesImage(img, issues);
                imageList.add(issuesImage);
                issuesimageRepository.save(issuesImage);
            }
        }
        Boolean isLike = issuesLikeRepository.existsByIssuesAndMember(issues, member);
        String avatarUrl = member.getAvatarUrl();
        return new IssuesResponseDto(issues,avatarUrl, imageList, isLike,issues.getIssuesLikeCnt(), opt);
    }

    //사진과 게시글 삭제
    public void deleteImgAndIssues(Issues issues){
        List<IssuesImage> imageList = issues.getIssuesimages();
        for (IssuesImage issuesImage : imageList) {
            s3UploadUtil.delete(issuesImage.getImgKey());
        }
        issuesRepository.delete(issues);
    }

    //반복되는 조회로직
    public IssuesResponseDto putImgsAndLikeToDto(Issues issues, Member member){
        List<IssuesImage> imgList = new ArrayList<>();
        for(IssuesImage img:issues.getIssuesimages()){
            imgList.add(img);
        }
        Boolean isLike = issuesLikeRepository.existsByIssuesAndMember(issues, member);
        String avatarUrl = member.getAvatarUrl();
        return new IssuesResponseDto(issues, avatarUrl, imgList, isLike, issues.getIssuesLikeCnt(), issues.getOpt());
    }
}