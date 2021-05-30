package by.book.service;

import by.book.dao.AddressDao;
import by.book.dao.UserDao;
import by.book.dao.inmemory.InMemoryAddressDao;
import by.book.dao.inmemory.InMemoryUserDao;
import by.book.dao.postgres.PgUserDao;
import by.book.entity.Address;
import by.book.entity.Role;
import by.book.entity.User;
import by.book.exception.DaoException;
import by.book.exception.UserDataException;

import java.time.LocalDate;
import java.util.List;

public class UserService {
    private UserDao userDao = new PgUserDao();
    private AddressDao addressDao = new InMemoryAddressDao();

    public void add(String userName, String firstName, String lastName, LocalDate birthDate, Address address, String password) throws UserDataException {
        if (userName == null || firstName == null || lastName == null || birthDate == null || address == null || password == null) {
            throw new UserDataException("Заполните все поля!");
        }
        if (password.length() <= 3) {
            throw new UserDataException("Длина пароля менее 3");
        }
        if (userDao.containsByName(userName)) {
            throw new UserDataException("Пользователь уже существует!");
        }
        userDao.save(new User(0, userName, firstName, lastName, birthDate, address, password, Role.USER));
    }


    public User getByUserName(String login) {
        if (userDao.containsByName(login)) {
            return userDao.getByUserName(login);
        }
        return null;
    }

    public boolean authCheck(String login, String password) {
        User user = userDao.getByUserName(login);
        if (user == null) {
            return false;
        }
        if (user.getPassword().equals(password)){
            return true;
        }
        return false;
    }
    public List<User> get(){
        return userDao.getAll();
    }

    public void changPassword(User user, String oldPassword, String newPassword, String confNewPassword)  throws UserDataException {
        if(!(user.getPassword().equals(oldPassword))){
            throw new UserDataException("Неверный старый пароль");
        }
        if (oldPassword == null || newPassword == null || confNewPassword == null ) {
            throw new UserDataException("Заполните все поля!");
        }
        if (newPassword.length() <= 3) {
            throw new UserDataException("Длина пароля пароль затем 3");
        }
        if(oldPassword.equals(newPassword)){
            throw  new UserDataException("Новый пароль такой же, как и старый");
        }
        if(!(newPassword.equals(confNewPassword))){
            throw  new UserDataException("Неверно введено подтверждение нового пароля");
        }
        userDao.updatePassword(user.getId(), newPassword);
    }

    public void changAddress(Address address, String street, int home) throws UserDataException {
        if (street == null || home < 1) {
            throw new UserDataException("Заполните все поля!");
        }
        if (street.equals(address.getStreet())  && home == address.getHome()) {
            throw new UserDataException("Новый адрес совпадает со старым");
        }
            address.setStreet(street);
            address.setHome(home);
    }

    public void changPersonalData(User user, String firstName,
                                  String lastName, LocalDate birthDate) throws UserDataException{

        if(firstName == null || lastName == null || birthDate == null){
            throw  new UserDataException("Заполните все поля!");
        }

        User cloneUser = userCreate(
                user.getId(), user.getUsername(),
                firstName, lastName,
                birthDate, user.getAddress(),
                user.getPassword(), user.getRole());

        if(user.equals(cloneUser)){
            throw  new UserDataException("Вы не сделали никаких изменений");
        }

        if (!user.getFirstName().equals(firstName)){
            userDao.updateFirstName( user.getId(),firstName);
        }
        if (!user.getLastName().equals(lastName)){
            userDao.updateLastName( user.getId(),lastName);
        }
        if (!user.getBirthDate().equals(birthDate)){
            userDao.updateBirthday( user.getId(),birthDate);
        }
    }

    private User userCreate(long id, String userName,
                            String firstName, String lastName,
                            LocalDate birthDate, Address address,
                            String password, Role role){

        return new User(id, userName, firstName, lastName, birthDate, address, password, role);
    }


}
