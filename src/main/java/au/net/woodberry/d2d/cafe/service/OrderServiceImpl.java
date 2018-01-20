package au.net.woodberry.d2d.cafe.service;

import au.net.woodberry.d2d.cafe.domain.MenuItem;
import au.net.woodberry.d2d.cafe.domain.MenuItemExtra;
import au.net.woodberry.d2d.cafe.domain.MenuItemSize;
import au.net.woodberry.d2d.cafe.exception.UnableToFulfilOrderException;
import au.net.woodberry.d2d.cafe.repository.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderServiceImpl implements OrderService {

    private final Repository<MenuItem> menuItemRepository;
    private final Repository<MenuItemExtra> menuItemExtraRepository;
    private final Repository<MenuItemSize> menuItemSizeRepository;

    public OrderServiceImpl(Repository<MenuItem> menuItemRepository,
                            Repository<MenuItemExtra> menuItemExtraRepository,
                            Repository<MenuItemSize> menuItemSizeRepository) {
        this.menuItemRepository = menuItemRepository;
        this.menuItemExtraRepository = menuItemExtraRepository;
        this.menuItemSizeRepository = menuItemSizeRepository;
    }

    @Override
    public List<MenuItem> prepareOrder(String item, String preparation, String... optionalExtras) throws UnableToFulfilOrderException {

        List<MenuItem> order = new ArrayList<>();

        // Retrieve all item information from the repository
        MenuItem menuItem = menuItemRepository.findByValue(item);
        if (menuItem == null) {
            throw new UnableToFulfilOrderException("Could not find menu item: '" + item + "'");
        }
        order.add(menuItem);

        MenuItemExtra menuItemPreparation = menuItemExtraRepository.findByValue(preparation);
        if (menuItemPreparation == null) {
            throw new UnableToFulfilOrderException("Could not select preparation.");
        }
        // Check that it is a preparation type
        if (!menuItemPreparation.getExtraType().equals(MenuItemExtra.Type.PREPARATION)) {
            throw new UnableToFulfilOrderException("Could not select preparation.");
        }
        order.add(menuItemPreparation);

        // Optional condiments - can be empty.
        List<MenuItemExtra> menuItemExtras = menuItemExtraRepository.findByValuesIn(optionalExtras);
        if (!menuItemExtras.isEmpty()) {
            menuItemExtras.addAll(menuItemExtras);
        }
        return order;
    }

    @Override
    public BigDecimal getTotalCost(List<MenuItem> menuItems, String sizeName) throws UnableToFulfilOrderException {
        // Return the total cost, sum the menu item, extras then multiply by the size.
        MenuItemSize menuItemSize = menuItemSizeRepository.findByValue(sizeName);
        if (menuItemSize == null) {
            throw new UnableToFulfilOrderException("No itemsize ");
        }
        return menuItems.stream()
                .map(MenuItem::getPrice)
                .reduce((p1, p2) -> p1.add(p2))
                .get()
                .multiply(menuItemSize.getMultiplier());
    }
}