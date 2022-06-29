package wafflepop

import java.nio.*
import org.lwjgl.*
import org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions
import org.lwjgl.system.Configuration.DEBUG
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.*
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.EXTDebugUtils.*
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkInstance
import scala.collection.JavaConverters._
import scala.util.Using

object ValidationLayers {
  val ENABLE_VALIDATION_LAYERS               = DEBUG.get(true)
  private val VALIDATION_LAYERS: Set[String] = Set("VK_LAYER_KHRONOS_validation")

  def debugCallback(
      messageSeverity: Int,
      messageType: Int,
      pCallbackData: Long,
      pUserData: Long
  ): Int = {
    val callbackData: VkDebugUtilsMessengerCallbackDataEXT =
      VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData)
    System.err.println("Validation layer: " + callbackData.pMessageString())
    VK_FALSE
  }

  def createDebugUtilsMessengerEXT(
      instance: VkInstance,
      createInfo: VkDebugUtilsMessengerCreateInfoEXT,
      allocationCallbacks: VkAllocationCallbacks,
      pDebugMessenger: LongBuffer
  ): Int = {
    if (vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") != NULL)
      vkCreateDebugUtilsMessengerEXT(instance, createInfo, allocationCallbacks, pDebugMessenger)
    else VK_ERROR_EXTENSION_NOT_PRESENT;
  }

  def destroyDebugUtilsMessengerEXT(
      instance: VkInstance,
      debugMessenger: Long,
      allocationCallbacks: VkAllocationCallbacks
  ): Unit =
    if (vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != NULL)
      vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, allocationCallbacks)

  def populateDebugMessengerCreateInfo(
      debugCreateInfo: VkDebugUtilsMessengerCreateInfoEXT
  ): Unit = {
    debugCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
    debugCreateInfo.messageSeverity(
      VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT
    );
    debugCreateInfo.messageType(
      VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT
    );
    debugCreateInfo.pfnUserCallback(debugCallback);
  }

  def setupDebugMessenger(instance: VkInstance): Long = {
    if (ENABLE_VALIDATION_LAYERS) {
      Using(stackPush) { stack =>

        val createInfo: VkDebugUtilsMessengerCreateInfoEXT =
          VkDebugUtilsMessengerCreateInfoEXT.callocStack(stack)
        populateDebugMessengerCreateInfo(createInfo)

        val pDebugMessenger: LongBuffer = stack.longs(VK_NULL_HANDLE)
        if (
          createDebugUtilsMessengerEXT(instance, createInfo, null, pDebugMessenger) != VK_SUCCESS
        ) {
          throw new RuntimeException("Failed to set up debug messenger")
        }
        pDebugMessenger.get(0)
      }.get
    } else { -1 }
  }

  def validationLayersAsPointerBuffer: PointerBuffer = {
    val stack: MemoryStack    = stackGet();
    val buffer: PointerBuffer = stack.mallocPointer(VALIDATION_LAYERS.size);

    VALIDATION_LAYERS.map(stack.UTF8(_)).foreach(buffer.put(_))

    buffer.rewind()
  }

  def getRequiredExtensions: PointerBuffer = {
    val glfwExtensions: PointerBuffer = glfwGetRequiredInstanceExtensions()

    if (ENABLE_VALIDATION_LAYERS) {
      val stack: MemoryStack        = stackGet()
      val extensions: PointerBuffer = stack.mallocPointer(glfwExtensions.capacity() + 1)

      // TODO: Test this cast
      extensions.put(glfwExtensions.asInstanceOf[org.lwjgl.system.Pointer])
      extensions.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME))

      // Rewind the buffer before returning it to reset its position back to 0
      return extensions.rewind()
    }

    glfwExtensions
  }

  def checkValidationLayerSupport: Boolean = {
    Using(stackPush) { stack =>
      val layerCount: IntBuffer = stack.ints(0);
      vkEnumerateInstanceLayerProperties(layerCount, null);

      val availableLayers: VkLayerProperties.Buffer =
        VkLayerProperties.mallocStack(layerCount.get(0), stack);

      vkEnumerateInstanceLayerProperties(layerCount, availableLayers);

      val availableLayerNames = availableLayers.asScala
        .map(_.layerNameString)
        .toSet

      VALIDATION_LAYERS.forall(availableLayerNames.contains(_))
    }.get
  }

}
