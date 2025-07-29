package Shared.Database.Repository;


import Shared.Database.DAO.GenericDAO;
import Shared.Models.Setting.Setting;

import java.util.UUID;

public class SettingRepository extends Repository<Setting, UUID> {
    public SettingRepository(GenericDAO<Setting> primaryDAO, GenericDAO<Setting> cacheDAO) {
        super(Setting.class, primaryDAO, cacheDAO);
    }
}