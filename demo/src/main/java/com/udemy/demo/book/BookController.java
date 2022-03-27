package com.udemy.demo.book;

import java.lang.reflect.Array;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import com.udemy.demo.borrow.Borrow;
import com.udemy.demo.borrow.BorrowRepository;
import com.udemy.demo.configuration.MyUserDetailService;
import com.udemy.demo.user.User;
import com.udemy.demo.user.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookController {

    /*
     * Méthode pour avoir Id de l'user connecté
     */
    public static Integer getConnectedUserId(Principal principal){
        if(!(principal instanceof UsernamePasswordAuthenticationToken )){
            throw new RuntimeException(("User not found"));
        }
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        Integer userId = ((MyUserDetailService.UserPrincipal) token.getPrincipal()).getUser().getId();
        return userId;
    }
    /*#########################################*/

    /*
    * DAO
    */
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BorrowRepository borrowRepository;
    /*#########################################*/

    
    /**
     * 
     * @param status
     * @return les livres de l'utilisateur ou les livres disponibles à l'emprunt
     */
    @GetMapping(value="/books")
    public ResponseEntity listBooks(@RequestParam(required=false) BookStatus status, Principal principal){

        List<Book> books;
        //Si status = FREE 
        if(status != null && status == BookStatus.FREE){
            //on retourne livre disponible à l'emprunt
            books = bookRepository.findByBookStatusAndUserIdNotAndDeletedFalse(status, this.getConnectedUserId(principal));
        }
        //Sinon 
        else{
            //On retourne livre de l'utilisateur
            books =  bookRepository.findByUserIdAndDeletedFalse(this.getConnectedUserId(principal));
        }
        return new ResponseEntity(books, HttpStatus.OK);
    }

    /**
     * 
     * @param bookId
     * @return un livre dont l'id est passé en param
     */
    @GetMapping("/books/{bookId}")
    public ResponseEntity loadBook(@PathVariable("bookId") String bookId ){
        Optional<Book> book = bookRepository.findById(Integer.valueOf(bookId));
        //On verifie que le livre demandé existe
        if(!book.isPresent()){
            return new ResponseEntity("ce livre n'existe pas", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(book.get(), HttpStatus.OK);
    }

    /**
     * Permet de sauver un livre en bse de donnée
     * @param book
     * @return le livre qui a été enregistré
     */
    @PostMapping(value="/books")
    public ResponseEntity addBook(@RequestBody @Valid Book book, Principal principal){
        Integer userConnectedId = this.getConnectedUserId(principal);

        //on récupére category à partir de l'id
        Optional<Category> category = categoryRepository.findById(book.getCategoryId());
        if(category.isPresent()){
            //Si category existe --> on initialise la category du book
            book.setCategory(category.get());
        }else{
            return new ResponseEntity("Vous devez fournir une category valide", HttpStatus.BAD_REQUEST);
        }

        Optional<User> user = userRepository.findById(userConnectedId);
        if(user.isPresent()){
            //Si user existe on initialise le user (propriétaire)
            book.setUser(user.get());
        }else{
            return new ResponseEntity("Vous devez fournir un user valide", HttpStatus.BAD_REQUEST);
        }
        //On met la valeur par défault aux attributs restant
        book.setDeleted(false);
        book.setStatus(BookStatus.FREE);

        bookRepository.save(book);

        return new ResponseEntity(book, HttpStatus.CREATED);
    }

    /**
     * Permet de chnger l'état  d'un livre (StatusBook = DELETED)
     * @param bookId
     * @return void
     */
    @DeleteMapping(value = "/books/{bookId}")
    public ResponseEntity deleteBook(@PathVariable("bookId") String bookId) {
        Optional<Book> bookToDelete = bookRepository.findById(Integer.valueOf(bookId));

        if (!bookToDelete.isPresent()) {
            return new ResponseEntity("Book not found", HttpStatus.BAD_REQUEST);
        }

        Book updatedBook = bookToDelete.get();
        List<Borrow> borrows = borrowRepository.findByBookId(updatedBook.getId());

        for (Borrow borrow : borrows) {
            if (borrow.getCloseDate() == null) {
                User borrower = borrow.getBorrower();
                return new ResponseEntity(borrower, HttpStatus.CONFLICT);
            }

        }
        updatedBook.setDeleted(true);
        bookRepository.save(updatedBook);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    //PathVariable url/{id} --> pour acceder à ressource spring
    //RequestParam url/book?name=toto --> mecanisme trie et pagination

    /**
     * Permet de modifier un livre
     * @param bookId
     * @param book
     * @return le livre modifié
     */
    @PutMapping(value = "/books/{bookId}")
    public ResponseEntity updateBook(@PathVariable("bookId") String bookId, @Valid @RequestBody Book book) {
        Optional<Book> bookToUpdate = bookRepository.findById(Integer.valueOf(bookId));
        if (!bookToUpdate.isPresent()) {
            return new ResponseEntity("Book not existing", HttpStatus.BAD_REQUEST);
        }
        Book bookToSave = bookToUpdate.get();
        Optional<Category> newCategory = categoryRepository.findById(book.getCategoryId());
        bookToSave.setCategory(newCategory.get());
        bookToSave.setTitle(book.getTitle());
        bookRepository.save(bookToSave);

        return new ResponseEntity(bookToSave, HttpStatus.OK);
    }

    /**
     * 
     * @return la liste de toute les catégory
     */
    @GetMapping("/categories")
    public ResponseEntity listCategories() {
        return new ResponseEntity(categoryRepository.findAll(), HttpStatus.OK);
    }
    
}
