package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.Comment;
import tn.fst.backend.backend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    @Override
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    @Override
    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public Comment updateComment(Long id, Comment commentDetails) {
        Optional<Comment> optional = commentRepository.findById(id);
        if (!optional.isPresent()) throw new RuntimeException("Comment not found with id: " + id);

        Comment comment = optional.get();
        comment.setName(commentDetails.getName());
        comment.setEmail(commentDetails.getEmail());
        comment.setComment(commentDetails.getComment());
        comment.setStatus(commentDetails.getStatus());

        return commentRepository.save(comment);
    }

    @Override
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) throw new RuntimeException("Comment not found with id: " + id);
        commentRepository.deleteById(id);
    }
}
