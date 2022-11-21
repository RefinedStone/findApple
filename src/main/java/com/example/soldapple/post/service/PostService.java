package com.example.soldapple.post.service;


import com.example.soldapple.aws_s3.S3UploadUtil;
import com.example.soldapple.create_price.dto.GetIPhonePriceResDto;
import com.example.soldapple.create_price.dto.GetMacbookPriceResDto;
import com.example.soldapple.like.repository.LikeRepository;
import com.example.soldapple.member.entity.Member;
import com.example.soldapple.post.dto.PostReqDto;
import com.example.soldapple.post.dto.PostResponseDto;
import com.example.soldapple.post.entity.Image;
import com.example.soldapple.post.entity.Opt;
import com.example.soldapple.post.entity.Post;
import com.example.soldapple.post.repository.ImageRepository;
import com.example.soldapple.post.repository.OptionRepository;
import com.example.soldapple.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    private final S3UploadUtil s3UploadUtil;
    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final OptionRepository optionRepository;


    //게시글 작성
    @Transactional
    public PostResponseDto postCreate(List<MultipartFile> multipartFiles,
                                      PostReqDto postReqDto,
                                      GetIPhonePriceResDto iphoneOption,
                                      GetMacbookPriceResDto macbookOption,
                                      Member member) throws IOException {
        //게시글 저장
        Post post = new Post(postReqDto, member);
        postRepository.save(post);

        /*옵션항목들 저장*/
        //맥북일 때
        if (iphoneOption == null) {
            Opt options = new Opt(macbookOption, post);
            optionRepository.save(options);
            return imgSave(multipartFiles, post, member, options);
        } else {
            //아이폰일 때
            Opt options = new Opt(iphoneOption, post);
            optionRepository.save(options);
            return imgSave(multipartFiles, post, member, options);
        }
    }
//    public void postTest(){
//        System.out.println("테스트성공");
//    }

    //게시글 수정
    @Transactional
    public PostResponseDto updatePost(List<MultipartFile> multipartFiles, Long postId, PostReqDto postReqDto, Member member) throws IOException {
        Post post = postRepository.findByPostIdAndMember(postId, member).orElseThrow(
                () -> new IllegalArgumentException("해당 게시글이 존재하지 않거나 수정 권한이 없습니다.")
        );

        deleteImg(post);
        post.update(postReqDto);
        return imgSave(multipartFiles, post, member, post.getOpt());
    }

    //게시글 삭제
    @Transactional
    public String postDelete(Long postId, Member member){
        Post post = postRepository.findByPostIdAndMember(postId,member).orElseThrow(
                () -> new RuntimeException("해당 게시글이 존재하지 않거나 삭제 권한이 없습니다.")
        );
        deleteImg(post);
        postRepository.deleteById(postId);
        return "게시글 삭제 완료";
    }

    //게시글 전체 조회 무한스크롤
    public Page<PostResponseDto> getAllPost(Pageable pageable) {
        return postRepository.findMyQuery(pageable);
    }

    //category + 내 좋아요 무한스크롤
    public Page<?> getAllPostWithCategory(Pageable pageable, String category) {
        Page<?> allPostWithCategory = postRepository.findAllPostWithCategory(pageable, category);
        return allPostWithCategory;
    }

    //category + 검색 정렬
    public Page<?> getAllPostWithCategoryWithSearch(Pageable pageable, String category, String search) {
        Page<?> allPostWithCategoryWithSearch = postRepository.findAllPostWithCategoryWithSearch(pageable, category, search);
        return allPostWithCategoryWithSearch;
    }

    //게시글 하나 조회
    public PostResponseDto onePost(Long postId, Member member) {
        Post post = postRepository.findByPostId(postId).orElseThrow(
                ()->new RuntimeException("해당 게시글이 존재하지 않습니다.")
        );
        Boolean isLike = likeRepository.existsByMemberAndPost(member, post);
        return new PostResponseDto(post, isLike);
    }

    //이미지 저장
    public PostResponseDto imgSave(List<MultipartFile> multipartFiles, Post post, Member member, Opt options) throws IOException {
        List<Image> imageList = new ArrayList<>();

        if(!(multipartFiles.size()==0)){
            System.out.println(multipartFiles.get(0).getOriginalFilename());
            for(MultipartFile imgFile : multipartFiles){
                Map<String, String> img = s3UploadUtil.upload(imgFile, "test");
                Image image = new Image(img, post);
                imageRepository.save(image);
                imageList.add(image);
                imageRepository.save(image);
            }
        }
        post.setImages(imageList);
        post.setOpt(options);
        Boolean isLike = likeRepository.existsByMemberAndPost(member, post);
        return new PostResponseDto(post, isLike);
    }

    //기존 사진 삭제
    public void deleteImg(Post post){
        List<Image> imageList = post.getImages();
        for (Image image : imageList) {
            s3UploadUtil.delete(image.getImgKey());
            imageRepository.delete(image);
        }
    }
}