package Shared.Database.Repository;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Media.Media;

import java.util.UUID;

public class MediaRepository extends Repository<Media, UUID> {
    public MediaRepository(GenericDAO<Media> primaryDAO, GenericDAO<Media> cacheDAO) {
        super(Media.class, primaryDAO, cacheDAO);
    }
}