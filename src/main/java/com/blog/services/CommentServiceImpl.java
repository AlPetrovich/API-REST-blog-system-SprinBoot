package com.blog.services;
import com.blog.dtos.CommentDTO;
import com.blog.entities.Comment;
import com.blog.entities.Publication;
import com.blog.exceptions.BlogAppException;
import com.blog.exceptions.ResourceNotFoundException;
import com.blog.repositories.CommentRepository;
import com.blog.repositories.PublicationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepo;
    @Autowired
    private PublicationRepository publicationRepo;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CommentDTO createComment(Long publicationId, CommentDTO commentDTO) {
        //1-Mapeamos a entidad para guardar en base de datos
        Comment comment = mapEntity(commentDTO);
        //2-Buscamos la publicacion
        Publication publication = publicationRepo.findById(publicationId)
                .orElseThrow(()-> new ResourceNotFoundException("Publication","id",publicationId));
        //3-Seteamos a la publicacion el nuevo comentario
        comment.setPublication(publication);
        //4-Guardamos comentario (entidad) en base de datos
        Comment newComent = commentRepo.save(comment);
        //5-Retornamos nuevo comentarioDTO
        return mapDTO(newComent);
    }

    @Override
    public List<CommentDTO> getCommentsByPublicationId(Long publicationId) {
        List<Comment> commentList = commentRepo.findByPublicationId(publicationId);
        return commentList.stream().map(comment -> mapDTO(comment)).collect(Collectors.toList());
    }

    @Override
    public CommentDTO findCommentById(Long publicationId ,Long commentId) {

        Publication publication = publicationRepo.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publication", "id", publicationId));

        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        //validation
        //si el id de la publicacion del comentario no es igual al id de la publicacion
        if( !comment.getPublication().getId().equals(publication.getId())){
            throw new BlogAppException(HttpStatus.BAD_REQUEST, "Comment does not belong to the post");
        }

        return mapDTO(comment);
    }

    @Override
    public CommentDTO updateComment(Long publicationId,Long commentId, CommentDTO commentDTO) {
        Publication publication = publicationRepo.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publication", "id", publicationId));

        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id",commentId));

        //validation
        //si el id de la publicacion del comentario no es igual al id de la publicacion
        if( !comment.getPublication().getId().equals(publication.getId())){
            throw new BlogAppException(HttpStatus.BAD_REQUEST, "Comment does not belong to the post");
        }

        comment.setName(commentDTO.getName());
        comment.setMail(commentDTO.getMail());
        comment.setBody(commentDTO.getBody());

        Comment commentUpdated = commentRepo.save(comment);
        return mapDTO(commentUpdated);
    }

    @Override
    public void deleteComment(Long publicationId, Long commentId) {
        Publication publication = publicationRepo.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publication", "id", publicationId));

        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id",commentId));

        if( !comment.getPublication().getId().equals(publication.getId())){
            throw new BlogAppException(HttpStatus.BAD_REQUEST, "Comment does not belong to the post");
        }

        commentRepo.delete(comment);
    }

    //------------------MODEL MAPPER---------------------
    private CommentDTO mapDTO(Comment comment){
        CommentDTO commentDTO = modelMapper.map(comment, CommentDTO.class);
        return commentDTO;
    }

    private Comment mapEntity(CommentDTO commentDTO){
        Comment comment = modelMapper.map(commentDTO, Comment.class);
        return comment;
    }
}
