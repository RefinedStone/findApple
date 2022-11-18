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

        //맥북일 때
        if (iphoneOption==null){
            Opt options = new Opt(macbookOption, post);
            optionRepository.save(options);
        } else{
            //아이폰일 때
            Opt options = new Opt(iphoneOption, post);
            optionRepository.save(options);
        }
        return imgSave(multipartFiles, post, member);
    }
//    public void postTest(){
//        System.out.println("테스트성공");
//    }

    //게시글 수정
    @Transactional
    public PostResponseDto updatePost(List<MultipartFile> multipartFiles, Long postId, PostReqDto postReqDto, Member member)throws IOException{
        Post post = postRepository.findByPostIdAndMember(postId, member).orElseThrow(
                () -> new IllegalArgumentException("해당 게시글이 존재하지 않거나 수정 권한이 없습니다.")
        );

        deleteImg(post);
        post.update(postReqDto);
        return imgSave(multipartFiles, post, member);
    }

    //게시글 삭제
    @Transactional
    public String postDelete(Long postId, Member member){
        Post post = postRepository.findByPostIdAndMember(postId,member).orElseThrow(
                () -> new RuntimeException("해당 게시글이 존재하지 않거나 삭제 권한이 없습니다.")
        );
        deleteImg(post);
        postRepository.delete(post);
        return "게시글 삭제 완료";
    }

    //게시글 전체 조회 무한스크롤
    public Page<PostResponseDto> testGetAllPost(Pageable pageable) {
        return postRepository.findMyQuery(pageable);
    }

    //게시글 전체 조회
    public List<PostResponseDto> allPosts(Member member) {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        List<PostResponseDto> postResponseDtos = new ArrayList<PostResponseDto>();
        for (Post post : posts) {
            postResponseDtos.add(putImgsAndLikeToDto(post, member));
        }
        return postResponseDtos;
    }

    //게시글 하나 조회
    public PostResponseDto onePost(Long postId, Member member) {
        Post post = postRepository.findByPostId(postId).orElseThrow(
                ()->new RuntimeException("해당 게시글이 존재하지 않습니다.")
        );
        return putImgsAndLikeToDto(post, member);
    }

    //게시글 카테고리 조회
    public List<PostResponseDto> categoryPost(String category, Member member) {
        List<PostResponseDto> postResponseDtos = new ArrayList<PostResponseDto>();
        List<Post> posts = postRepository.findAllByCategoryOrderByCreatedAtDesc(category);
        for (Post post : posts) {
            postResponseDtos.add(putImgsAndLikeToDto(post, member));
        }
        return postResponseDtos;
    }

////반복되는 로직 메소드
    //이미지 저장
    public PostResponseDto imgSave(List<MultipartFile> multipartFiles, Post post, Member member) throws IOException{
        List<Image> imageList = new ArrayList<>();

        if(!(multipartFiles.size()==0)){
            System.out.println(multipartFiles.get(0).getOriginalFilename());
            for(MultipartFile imgFile : multipartFiles){
                Map<String, String> img = s3UploadUtil.upload(imgFile, "test");
                Image image = new Image(img, post);
                imageList.add(image);
                imageRepository.save(image);
            }
        }
        Boolean isLike = likeRepository.existsByMemberAndPost(member, post);
        return new PostResponseDto(post, imageList, isLike, post.getPostLikeCnt());
    }

    //기존 사진 삭제
    public void deleteImg(Post post){
        List<Image> imageList = post.getImages();
        for (Image image : imageList) {
//            imageRepository.delete(image);
            s3UploadUtil.delete(image.getImgKey());
        }
    }

    //반복되는 조회리스트 로직
    public PostResponseDto putImgsAndLikeToDto(Post post, Member member){
        List<Image> imgList = new ArrayList<>();
        for(Image img:post.getImages()){
            imgList.add(img);
        }
        Boolean isLike = likeRepository.existsByMemberAndPost(member, post);
        return new PostResponseDto(post,imgList,isLike,post.getPostLikeCnt());
    }
}